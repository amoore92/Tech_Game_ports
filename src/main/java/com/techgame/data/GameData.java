package com.techgame.data;

import java.util.ArrayList;
import java.util.List;

/** Parsers for the text/CSV data files. Pure functions over the file contents. */
public final class GameData
{
    private static final String BOM = "\uFEFF";

    private GameData ()
    {
    }

    /** Builds the round pools from the raw contents of the three data files. */
    public static Pools parsePools (String templateText, String nameText, String drugText)
    {
        return new Pools(parseTemplates(templateText), parseNames(nameText), parseDrugs(drugText));
    }

    //  Split text into trimmed, non-empty lines. trim() also strips the trailing
    //  '\r' that Windows line endings (\r\n) leave behind. JS's trim() also drops a
    //  leading UTF-8 BOM (the CSV has one); Java's doesn't, so strip it explicitly
    //  or the header row wouldn't be recognised.
    static List<String> toLines (String text)
    {
        if (text.startsWith(BOM))
        {
            text = text.substring(1);
        }
        List<String> lines = new ArrayList<>();
        for (String line : text.split("\n"))
        {
            String trimmed = line.trim();
            if (!trimmed.isEmpty())
            {
                lines.add(trimmed);
            }
        }
        return lines;
    }

    //  Greeting templates: keep ONLY lines that contain both NAME and DRUG,
    //  otherwise a generated greeting could be missing the drug clue.
    static List<String> parseTemplates (String templateText)
    {
        List<String> templates = new ArrayList<>();
        for (String line : toLines(templateText))
        {
            if (line.contains("NAME") && line.contains("DRUG"))
            {
                templates.add(line);
            }
        }
        return templates;
    }

    //  Names file is "First, Last" per line -> two independent pools.
    static Names parseNames (String nameText)
    {
        List<String> firstNames = new ArrayList<>();
        List<String> lastNames = new ArrayList<>();
        for (String line : toLines(nameText))
        {
            String[] parts = line.split(",");
            String first = parts[0].trim();
            String last = parts.length > 1 ? parts[1].trim() : "";
            if (!first.isEmpty()) firstNames.add(first);
            if (!last.isEmpty()) lastNames.add(last);
        }
        return new Names(firstNames, lastNames);
    }

    //  Split one CSV line into fields, respecting double-quoted values. The current
    //  data file has no in-field commas (multi-value columns use '/'), but this keeps
    //  parsing correct if quoted commas are ever added.
    static List<String> parseCsvLine (String line)
    {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++)
        {
            char ch = line.charAt(i);
            if (ch == '"')
            {
                //  Doubled "" inside a quoted field = a literal quote.
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"')
                {
                    current.append('"');
                    i++;
                }
                else
                {
                    inQuotes = !inQuotes;
                }
            }
            else if (ch == ',' && !inQuotes)
            {
                fields.add(current.toString().trim());
                current.setLength(0);
            }
            else
            {
                current.append(ch);
            }
        }
        fields.add(current.toString().trim());
        return fields;
    }

    //  Flashcard CSV -> one Drug per row. Columns (in order):
    //    Generic Name, Brand Name, Drug Class, Primary Indications,
    //    Common or Notable Side Effects, Warnings and Counseling Notes
    //  The first line is the header and is skipped.
    static List<Drug> parseDrugs (String drugText)
    {
        List<String> lines = toLines(drugText);
        int start = !lines.isEmpty() && lines.get(0).startsWith("Generic Name") ? 1 : 0;

        List<Drug> drugs = new ArrayList<>();
        for (String line : lines.subList(start, lines.size()))
        {
            List<String> f = parseCsvLine(line);
            Drug drug = new Drug(field(f, 0), field(f, 1), field(f, 2), field(f, 3), field(f, 4), field(f, 5));
            if (!drug.generic().isEmpty() && !drug.brand().isEmpty())
            {
                drugs.add(drug);
            }
        }
        return drugs;
    }

    private static String field (List<String> fields, int index)
    {
        return index < fields.size() ? fields.get(index) : "";
    }
}
