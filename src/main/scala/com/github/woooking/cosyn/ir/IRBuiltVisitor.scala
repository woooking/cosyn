package com.github.woooking.cosyn.ir

import com.github.javaparser.ast._
import com.github.javaparser.ast.`type`._
import com.github.javaparser.ast.body._
import com.github.javaparser.ast.comments.{BlockComment, JavadocComment, LineComment}

import scala.collection.JavaConverters._
import com.github.woooking.cosyn.util.OptionConverters._
import com.github.javaparser.ast.expr._
import com.github.javaparser.ast.modules._
import com.github.javaparser.ast.stmt._
import com.github.javaparser.ast.visitor.{GenericVisitor, GenericVisitorAdapter}
import com.github.woooking.cosyn.cfg.CFG
import com.github.woooking.cosyn.ir.statements._

class IRBuiltVisitor extends GenericVisitor[NodeResult, NodeArg] {

    // - Compilation Unit ----------------------------------
    override def visit(n: CompilationUnit, arg: NodeArg): NodeResult = {
        n.getTypes.accept(this, arg)
    }

    override def visit(n: PackageDeclaration, arg: NodeArg): NodeResult = ???

    override def visit(n: TypeParameter, arg: NodeArg): NodeResult = ???

    override def visit(n: LineComment, arg: NodeArg): NodeResult = ???

    override def visit(n: BlockComment, arg: NodeArg): NodeResult = ???

    // - Body ----------------------------------------------
    override def visit(n: ClassOrInterfaceDeclaration, arg: NodeArg): NodeResult = {
        n.getMembers.accept(this, arg)
    }

    override def visit(n: MethodDeclaration, arg: NodeArg): NodeResult = ???




    override def visit(n: ArrayAccessExpr, arg: NodeArg): NodeResult = {
        val array = n.getName.accept(this, arg).asInstanceOf[IRExpression]
        val index = n.getIndex.accept(this, arg).asInstanceOf[IRExpression]
//        val target = arg..createTempVar()
//        arg.block.addStatement(IRArrayAccess(target, array, index))
//        target
        NoResult
    }

    override def visit(n: ArrayCreationExpr, arg: NodeArg): NodeResult = {
        val sizes = n.getLevels.asScala.map(_.accept(this, arg).asInstanceOf[IRExpression])
        val initializers = n.getInitializer.asScala.map(_.getValues.asScala.map(_.accept(this, arg).asInstanceOf[IRExpression]))
//        val target = cfg.createTempVar()
//        arg.block.addStatement(IRArrayCreation(target, n.getElementType, sizes, initializers.toSeq.flatten))
//        target
        NoResult
    }

    override def visit(n: AssertStmt, arg: NodeArg): NodeResult = {
        val condition = n.getCheck.accept(this, arg).asInstanceOf[IRExpression]
        val message = n.getMessage.asScala.map(_.accept(this, arg).asInstanceOf[IRExpression])
//        arg.block.addStatement(IRAssert(condition, message))
        NoResult
    }

    override def visit(n: AssignExpr, arg: NodeArg): NodeResult = {
        val rhs = n.getValue.accept(this, arg).asInstanceOf[IRExpression]
//        val lhs = n.getTarget.accept(this, arg).asInstanceOf[IRVariable]
//        val statement = n.getOperator match {
//            case AssignExpr.Operator.ASSIGN => IRAssignment(lhs, rhs)
//            case ope => IRBinaryOperation(lhs, BinaryOperator.fromAssignExprOperator(ope), lhs, rhs)
//        }
//        arg.block.addStatement(statement)
//        lhs
        NoResult
    }

    override def visit(n: BinaryExpr, arg: NodeArg): NodeResult = {
        val lhs = n.getLeft.accept(this, arg).asInstanceOf[IRExpression]
        val rhs = n.getRight.accept(this, arg).asInstanceOf[IRExpression]
//        val target = cfg.createTempVar()
//        arg.block.addStatement(IRBinaryOperation(target, BinaryOperator.fromBinaryExprOperator(n.getOperator), lhs, rhs))
//        target
        NoResult
    }

    override def visit(n: BooleanLiteralExpr, arg: NodeArg): NodeResult = {
        if (n.getValue) IRExpression.True else IRExpression.False
    }

    override def visit(n: BreakStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: CatchClause, arg: NodeArg): NodeResult = ???

    override def visit(n: CharLiteralExpr, arg: NodeArg): NodeResult = {
        IRChar(n.getValue.charAt(0))
    }

    override def visit(n: ClassExpr, arg: NodeArg): NodeResult = {
//        val target = cfg.createTempVar()
//        arg.block.addStatement(IRClassExpr(target, n.getType))
//        target
        NoResult
    }

    override def visit(n: ConditionalExpr, arg: NodeArg): NodeResult = {
        val condition = n.getCondition.accept(this, arg).asInstanceOf[IRExpression]
        val thenExpr = n.getThenExpr.accept(this, arg).asInstanceOf[IRExpression]
        val elseExpr = n.getElseExpr.accept(this, arg).asInstanceOf[IRExpression]
//        val target = cfg.createTempVar()
//        arg.block.addStatement(IRConditionalExpr(target, condition, thenExpr, elseExpr))
//        target
        NoResult
    }

    override def visit(n: DoStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: ContinueStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: WhileStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: IfStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: ClassOrInterfaceType, arg: NodeArg): NodeResult = ???

    override def visit(n: JavadocComment, arg: NodeArg): NodeResult = ???

    override def visit(n: InitializerDeclaration, arg: NodeArg): NodeResult = ???

    override def visit(n: Parameter, arg: NodeArg): NodeResult = ???

    override def visit(n: EnumDeclaration, arg: NodeArg): NodeResult = ???

    override def visit(n: EnumConstantDeclaration, arg: NodeArg): NodeResult = ???

    override def visit(n: AnnotationDeclaration, arg: NodeArg): NodeResult = ???

    override def visit(n: AnnotationMemberDeclaration, arg: NodeArg): NodeResult = ???

    override def visit(n: FieldDeclaration, arg: NodeArg): NodeResult = ???

    override def visit(n: VariableDeclarator, arg: NodeArg): NodeResult = ???

    override def visit(n: ConstructorDeclaration, arg: NodeArg): NodeResult = ???

    override def visit(n: UnknownType, arg: NodeArg): NodeResult = ???

    override def visit(n: ArrayInitializerExpr, arg: NodeArg): NodeResult = ???

    override def visit(n: ThisExpr, arg: NodeArg): NodeResult = ???

    override def visit(n: SuperExpr, arg: NodeArg): NodeResult = ???

    override def visit(n: ObjectCreationExpr, arg: NodeArg): NodeResult = ???

    override def visit(n: NameExpr, arg: NodeArg): NodeResult = ???

    override def visit(n: MethodCallExpr, arg: NodeArg): NodeResult = ???

    override def visit(n: NullLiteralExpr, arg: NodeArg): NodeResult = ???

    override def visit(n: DoubleLiteralExpr, arg: NodeArg): NodeResult = ???

    override def visit(n: Name, arg: NodeArg): NodeResult = ???

    override def visit(n: NodeList[_ <: Node], arg: NodeArg): NodeResult = {
        ListResult(n.asScala.map(_.accept(this, arg)))
    }

    override def visit(n: TypeExpr, arg: NodeArg): NodeResult = ???

    override def visit(n: ReturnStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: SwitchEntryStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: SwitchStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: ExpressionStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: EmptyStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: LabeledStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: BlockStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: LocalClassDeclarationStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: ExplicitConstructorInvocationStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: PrimitiveType, arg: NodeArg): NodeResult = ???

    override def visit(n: ArrayType, arg: NodeArg): NodeResult = ???

    override def visit(n: ArrayCreationLevel, arg: NodeArg): NodeResult = ???

    override def visit(n: IntersectionType, arg: NodeArg): NodeResult = ???

    override def visit(n: UnionType, arg: NodeArg): NodeResult = ???

    override def visit(n: VoidType, arg: NodeArg): NodeResult = ???

    override def visit(n: WildcardType, arg: NodeArg): NodeResult = ???

    override def visit(n: UnaryExpr, arg: NodeArg): NodeResult = ???

    override def visit(n: VariableDeclarationExpr, arg: NodeArg): NodeResult = ???

    override def visit(n: MarkerAnnotationExpr, arg: NodeArg): NodeResult = ???

    override def visit(n: SingleMemberAnnotationExpr, arg: NodeArg): NodeResult = ???

    override def visit(n: NormalAnnotationExpr, arg: NodeArg): NodeResult = ???

    override def visit(n: MemberValuePair, arg: NodeArg): NodeResult = ???

    override def visit(n: SimpleName, arg: NodeArg): NodeResult = ???

    override def visit(n: ImportDeclaration, arg: NodeArg): NodeResult = ???

    override def visit(n: ModuleDeclaration, arg: NodeArg): NodeResult = ???

    override def visit(n: ModuleRequiresStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: ModuleExportsStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: ModuleProvidesStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: ModuleUsesStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: ModuleOpensStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: UnparsableStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: ReceiverParameter, arg: NodeArg): NodeResult = ???

    override def visit(n: MethodReferenceExpr, arg: NodeArg): NodeResult = ???

    override def visit(n: LambdaExpr, arg: NodeArg): NodeResult = ???

    override def visit(n: TryStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: SynchronizedStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: ThrowStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: ForStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: ForeachStmt, arg: NodeArg): NodeResult = ???

    override def visit(n: CastExpr, arg: NodeArg): NodeResult = ???

    override def visit(n: EnclosedExpr, arg: NodeArg): NodeResult = ???

    override def visit(n: FieldAccessExpr, arg: NodeArg): NodeResult = ???

    override def visit(n: InstanceOfExpr, arg: NodeArg): NodeResult = ???

    override def visit(n: StringLiteralExpr, arg: NodeArg): NodeResult = ???

    override def visit(n: IntegerLiteralExpr, arg: NodeArg): NodeResult = ???

    override def visit(n: LongLiteralExpr, arg: NodeArg): NodeResult = ???
}
