package com.github.woooking.cosyn.ui

import java.io.{ByteArrayOutputStream, PrintStream}

import com.github.woooking.cosyn.cfg._

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.{ChoiceBox, Label, ScrollPane}
import Label._
import scalafx.scene.layout.{HBox, Pane, VBox}

class CFGViewer(code: String, methods: Map[String, CFG]) extends JFXApp {
    val statements = new Label("")

    class BlockID(block: CFGBlock) extends Label(block.id.toString) {
        onMouseClicked = () => {
            val bos = new ByteArrayOutputStream()
            val ps = new PrintStream(bos)
            block.print(ps)
            statements.text() = bos.toString()
        }
    }

    def generateView(cfg: CFG) = {
        def fromBlock(pane: Pane, block: CFGBlock, x: Int, y: Int, visited: Set[CFGBlock]): Set[CFGBlock] = {
            if (visited contains block) return visited
            pane.children.add(new BlockID(block) {
                layoutX = x.toDouble
                layoutY = y.toDouble
            })
            block match {
                case b: CFGStatements =>
                    b.next match {
                        case None =>
                            visited
                        case Some(n) =>
                            fromBlock(pane, n, x, y + 40, visited + block)
                    }
                case b: CFGBranch =>
                    val v = fromBlock(pane, b.thenBlock, x - 20, y + 40, visited + block)
                    fromBlock(pane, b.elseBlock, x + 20, y + 40, v + block)
                case _: CFGExit =>
                    visited
            }
        }

        val canvas = new Pane {
            minWidth = 400
            minHeight = 800
        }
        fromBlock(canvas, cfg.entry, 200, 20, Set.empty)
        canvas
    }

    stage = new JFXApp.PrimaryStage {
        title.value = "Hello Stage"
        width = 1024
        height = 768
        scene = new Scene {
            root = new HBox {
                children = Seq(
                    new ScrollPane {
                        content = Label(code)
                    },
                    new VBox {
                        minWidth = 400
                        minHeight = 800
                        val pane = new ScrollPane {
                            content = new Canvas
                        }
                        val choice = new ChoiceBox(ObservableBuffer(methods.keys.toSeq)) {
                            value.onChange(pane.content = generateView(methods(value.value)))
                        }
                        children = Seq(
                            choice,
                            pane,
                            statements
                        )
                    }
                )
            }
        }
    }
}