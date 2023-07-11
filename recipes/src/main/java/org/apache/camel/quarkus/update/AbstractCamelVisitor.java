package org.apache.camel.quarkus.update;

import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Parent of Camel visitors, skips visit methods in case that there is no camel package imported.
 */
public abstract class AbstractCamelVisitor extends JavaIsoVisitor<ExecutionContext> {

    //flag that camel package is imported to the file
    private boolean camel = false;

    private LinkedList<JavaType> implementsList = new LinkedList<>();

    @Override
    public final J.Import visitImport(J.Import _import, ExecutionContext context) {
        //if there is at least one import of camel class, the camel recipe should be executed
        if(_import.getTypeName().contains("org.apache.camel")) {
            camel = true;
        }
        return doVisitImport(_import, context);
    }

    @Override
    public final J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext context) {
        if (classDecl.getImplements() != null && !classDecl.getImplements().isEmpty()) {
            implementsList.addAll(classDecl.getImplements().stream().map(i -> i.getType()).collect(Collectors.toList()));
        }
        return doVisitClassDeclaration(classDecl, context);
    }

     //internal methods used by children

     J.Import doVisitImport(J.Import _import, ExecutionContext context) {
        return super.visitImport(_import, context);
     }

     J.ClassDeclaration doVisitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext context) {
        if(!camel) {
            //skip recipe if file does not contain camel
            return classDecl;
        }

        return super.visitClassDeclaration(classDecl, context);
     }

     // getters and setters


    public LinkedList<JavaType> getImplementsList() {
        return implementsList;
    }
}
