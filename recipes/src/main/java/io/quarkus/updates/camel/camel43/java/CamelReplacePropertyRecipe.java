package io.quarkus.updates.camel.camel43.java;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.binary.Binary;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.marker.AlreadyReplaced;
import org.openrewrite.marker.Marker;
import org.openrewrite.quark.Quark;
import org.openrewrite.remote.Remote;
import org.openrewrite.text.PlainText;
import org.openrewrite.text.PlainTextParser;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static org.openrewrite.Tree.randomId;

public class CamelReplacePropertyRecipe extends Recipe {

    String find = "camel.main.routeController.{1}";

    @Override
    public String getDisplayName() {
        return "Camel bean recipe";
    }

    @Override
    public String getDescription() {
        return "Camel bean recipe.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new TreeVisitor<Tree, ExecutionContext>() {
            @Override
            public Tree visit(@Nullable Tree tree, ExecutionContext ctx) {
                SourceFile sourceFile = (SourceFile) requireNonNull(tree);
                if (sourceFile instanceof Quark || sourceFile instanceof Remote || sourceFile instanceof Binary) {
                    return sourceFile;
                }
                for (Marker marker : sourceFile.getMarkers().getMarkers()) {
                    if (marker instanceof AlreadyReplaced) {
                        AlreadyReplaced alreadyReplaced = (AlreadyReplaced) marker;
                        if (Objects.equals(find, alreadyReplaced.getFind())) {
                            return sourceFile;
                        }
                    }
                }
                String searchStr = find;

                PlainText plainText = PlainTextParser.convert(sourceFile);
                Pattern pattern = Pattern.compile(searchStr, Pattern.DOTALL);
                Matcher matcher = pattern.matcher(plainText.getText());
//                String result = matcher.replaceAll(matchResult -> matchResult.group().substring(0, matchResult.group().length()-1) +
//                        matchResult.group().toLowerCase().substring(matchResult.group().length()-1, matchResult.group().length()));

                StringBuilder sb = new StringBuilder(plainText.getText());
                while (matcher.find()) {
                    String buf = "camel.routecontroller." + sb.substring(matcher.end()-1, matcher.end()).toLowerCase();
                    sb.replace(matcher.start(), matcher.end(), buf);
                }
                System.out.println(sb);

                return plainText.withText(sb.toString())
                        .withMarkers(sourceFile.getMarkers().add(new AlreadyReplaced(randomId(), find, "false")));
            }
        };
    }
}
