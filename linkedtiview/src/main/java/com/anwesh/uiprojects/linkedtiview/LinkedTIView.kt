package com.anwesh.uiprojects.linkedtiview

/**
 * Created by anweshmishra on 19/06/18.
 */

import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.*

val IT_NODES : Int = 5

class LinkedTIView (ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var prevScale : Float = 0f, var dir : Float = 0f, var j : Int = 0) {

        val scales : Array<Float> = arrayOf(0f, 0f)

        fun update(stopcb : (Float) -> Unit) {
            scales[j] += dir * 0.1f
            if (Math.abs(scales[j] - prevScale) > 1) {
                scales[j] = prevScale + dir
                j += dir.toInt()
                if (j == scales.size || j == -1) {
                    j -= dir.toInt()
                    dir = 0f
                    prevScale = scales[j]
                    stopcb(prevScale)
                }
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                startcb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch (ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class ITNode (var i : Int) {

        private val state : State = State()

        private var next : ITNode? = null

        private var prev : ITNode? = null

        fun draw(canvas : Canvas, paint : Paint) {
            val w : Float = canvas.width.toFloat()
            val h : Float = canvas.height.toFloat()
            val gap : Float = w / IT_NODES
            paint.strokeWidth = Math.min(w, h) / 60
            paint.strokeCap = Paint.Cap.ROUND
            paint.color = Color.WHITE
            canvas.save()
            canvas.translate(i * gap + gap * state.scales[0] - gap/3 , h / 2)
            for (i in 0..1) {
                canvas.save()
                canvas.rotate(i * 180F * state.scales[1])
                canvas.drawLine(0f, -gap/3, 0f, gap/3, paint)
                canvas.drawLine(-gap/6, -gap/3, gap/6, -gap/3, paint)
                canvas.restore()
            }
            canvas.restore()
        }

        fun update(stopcb : (Float) -> Unit) {
            state.update(stopcb)
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }

        init {
            addNeighbor()
        }

        fun getNext(dir : Int, cb : () -> Unit) : ITNode {
            var curr : ITNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }

        fun addNeighbor() {
            if (i < IT_NODES - 1) {
                next = ITNode(i + 1)
                next?.prev = this
            }
        }
    }

    data class LinkedIT(var i : Int) {

        private var curr : ITNode = ITNode(0)

        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(stopcb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                stopcb(it)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            curr.startUpdating(startcb)
        }
    }

    data class Renderer(var view : LinkedTIView) {

        private val animator : Animator = Animator(view)

        private val linkedIT : LinkedIT = LinkedIT(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            linkedIT.draw(canvas, paint)
            animator.animate {
                linkedIT.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            linkedIT.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            val view : LinkedTIView = LinkedTIView(activity)
            activity.setContentView(view)
        }
    }
}