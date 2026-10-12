package com.techgame.net;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Tiny client for the scores backend ({@code GET/POST <baseUrl>/api/scores}).
 *
 * <p>The web build used same-origin paths on Vercel; a desktop app has no origin,
 * so the deployment's base URL is passed in (see the launcher). Both calls FAIL
 * SOFT: if the URL is unset or the network/backend is down, the game keeps working
 * (you just get {@code false} / an empty list), never a crash.
 *
 * <p>Callbacks always run on the render thread.
 */
public final class ScoreApi
{
    private static final String TAG = "ScoreApi";
    private static final String PATH = "/api/scores";
    private static final int TIMEOUT_MS = 10_000;

    private final String baseUrl;

    public ScoreApi (String baseUrl)
    {
        String url = baseUrl == null ? "" : baseUrl.trim();
        while (url.endsWith("/"))
        {
            url = url.substring(0, url.length() - 1);
        }
        this.baseUrl = url;
    }

    public void submitScore (String name, int score, Consumer<Boolean> done)
    {
        if (baseUrl.isEmpty())
        {
            deliver(done, false);
            return;
        }

        JsonValue body = new JsonValue(JsonValue.ValueType.object);
        body.addChild("name", new JsonValue(name));
        body.addChild("score", new JsonValue(score));

        HttpRequest request = new HttpRequestBuilder().newRequest()
                .method(HttpMethods.POST)
                .url(baseUrl + PATH)
                .timeout(TIMEOUT_MS)
                .header("Content-Type", "application/json")
                .content(body.toJson(OutputType.json))
                .build();

        Gdx.net.sendHttpRequest(request, new HttpResponseListener()
        {
            @Override
            public void handleHttpResponse (HttpResponse response)
            {
                deliver(done, isOk(response));
            }

            @Override
            public void failed (Throwable t)
            {
                Gdx.app.error(TAG, "submitScore failed", t);
                deliver(done, false);
            }

            @Override
            public void cancelled ()
            {
                deliver(done, false);
            }
        });
    }

    public void getLeaderboard (Consumer<List<ScoreRow>> done)
    {
        if (baseUrl.isEmpty())
        {
            deliver(done, List.of());
            return;
        }

        HttpRequest request = new HttpRequestBuilder().newRequest()
                .method(HttpMethods.GET)
                .url(baseUrl + PATH)
                .timeout(TIMEOUT_MS)
                .build();

        Gdx.net.sendHttpRequest(request, new HttpResponseListener()
        {
            @Override
            public void handleHttpResponse (HttpResponse response)
            {
                //  The body must be read here, on the network thread.
                deliver(done, isOk(response) ? parseRows(response.getResultAsString()) : List.of());
            }

            @Override
            public void failed (Throwable t)
            {
                Gdx.app.error(TAG, "getLeaderboard failed", t);
                deliver(done, List.of());
            }

            @Override
            public void cancelled ()
            {
                deliver(done, List.of());
            }
        });
    }

    static List<ScoreRow> parseRows (String body)
    {
        try
        {
            JsonValue root = new JsonReader().parse(body);
            if (root == null || !root.isArray())
            {
                return List.of();
            }
            List<ScoreRow> rows = new ArrayList<>();
            for (JsonValue row = root.child; row != null; row = row.next)
            {
                rows.add(new ScoreRow(row.getString("name", ""), scoreText(row.get("score"))));
            }
            return rows;
        }
        catch (RuntimeException e)
        {
            Gdx.app.error(TAG, "Bad leaderboard response", e);
            return List.of();
        }
    }

    private static String scoreText (JsonValue score)
    {
        if (score == null) return "";
        return score.isLong() ? Long.toString(score.asLong()) : score.asString();
    }

    private static boolean isOk (HttpResponse response)
    {
        int code = response.getStatus().getStatusCode();
        return code >= 200 && code < 300;
    }

    private static <T> void deliver (Consumer<T> done, T value)
    {
        Gdx.app.postRunnable(() -> done.accept(value));
    }
}
