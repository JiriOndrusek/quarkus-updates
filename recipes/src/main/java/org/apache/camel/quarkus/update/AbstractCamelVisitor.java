package org.apache.camel.quarkus.update;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.internal.lang.Nullable;
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

    @Override
    public final J.FieldAccess visitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext context) {
        if(!camel) {
            //skip recipe if file does not contain camel
            return fieldAccess;
        }

        return doVisitFieldAccess(fieldAccess, context);
    }

    @Override
    public final J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext context) {
        if(!camel) {
            //skip recipe if file does not contain camel
            return method;
        }

        return doVisitMethodDeclaration(method, context);
    }

    @Override
    public final J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext context) {
        if(!camel) {
            //skip recipe if file does not contain camel
            return method;
        }

        return doVisitMethodInvocation(method, context);
    }

    @Override
    public final J.Annotation visitAnnotation(J.Annotation annotation, ExecutionContext context) {
        if(!camel) {
            //skip recipe if file does not contain camel
            return annotation;
        }
        return doVisitAnnotation(annotation, context);
    }

    @Override
    public final @Nullable J postVisit(J tree, ExecutionContext context) {
        if(!camel) {
            //skip recipe if file does not contain camel
            return tree;
        }

        return doPostVisit(tree, context);
    }

    //-------------------------------- internal methods used by children---------------------------------

     J.Import doVisitImport(J.Import _import, ExecutionContext context) {
        return super.visitImport(_import, context);
     }

     J.ClassDeclaration doVisitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext context) {
        return super.visitClassDeclaration(classDecl, context);
     }

    J.FieldAccess doVisitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext context) {
        return super.visitFieldAccess(fieldAccess, context);
    }

    J.MethodDeclaration doVisitMethodDeclaration(J.MethodDeclaration method, ExecutionContext context) {
        return super.visitMethodDeclaration(method, context);
    }

    J.MethodInvocation doVisitMethodInvocation(J.MethodInvocation method, ExecutionContext context) {
        return super.visitMethodInvocation(method, context);
    }

    J.Annotation doVisitAnnotation(J.Annotation annotation, ExecutionContext context) {
        return super.visitAnnotation(annotation, context);
    }

    @Nullable J doPostVisit(J tree, ExecutionContext context) {
        return super.postVisit(tree, context);
    }

     // ------------------------------------------ getters and setters -------------------------------------------


    public LinkedList<JavaType> getImplementsList() {
        return implementsList;
    }
}
