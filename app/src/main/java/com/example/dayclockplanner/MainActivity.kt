package com.example.dayclockplanner

import android.app.*
import android.os.Bundle
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.*
import java.util.*

data class Event(val title:String, val start:Float, val end:Float, val color:Int)

class MainActivity : Activity() {
    private lateinit var clock: DayClockView
    private val events = mutableListOf(
        Event("Schlaf", 0f, 7f, Color.rgb(118,118,170)),
        Event("Morgenroutine", 7f, 8f, Color.rgb(245,178,74)),
        Event("Schule", 8f, 14f, Color.rgb(82,150,230)),
        Event("Mittagessen", 14f, 15f, Color.rgb(105,190,135)),
        Event("Lernen", 15f, 17f, Color.rgb(224,104,132)),
        Event("Sport", 17f, 18.5f, Color.rgb(232,142,82)),
        Event("Freizeit", 18.5f, 22f, Color.rgb(145,111,205)),
        Event("Entspannen", 22f, 24f, Color.rgb(105,105,120))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clock = DayClockView()
        setContentView(clock)
    }

    inner class DayClockView : View(this) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var centerX=0f; private var centerY=0f; private var radius=0f
        private val hourLabels = arrayOf("00","03","06","09","12","15","18","21")
        private val hourPositions = floatArrayOf(0f,3f,6f,9f,12f,15f,18f,21f)

        init {
            setBackgroundColor(Color.rgb(247,247,248))
            textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
            setOnClickListener { addEventDialog() }
        }

        override fun onDraw(c: Canvas) {
            super.onDraw(c)
            centerX=width/2f
            centerY=height/2f - 10
            radius=minOf(width,height)*.39f

            // outer activity ring
            paint.style=Paint.Style.STROKE
            paint.strokeWidth=42f
            paint.strokeCap=Paint.Cap.BUTT
            for(e in events){
                paint.color=e.color
                val start=-90f + e.start/24f*360f
                val sweep=(e.end-e.start)/24f*360f
                c.drawArc(centerX-radius,centerY-radius,centerX+radius,centerY+radius,start,sweep,false,paint)
            }

            // clock face
            paint.style=Paint.Style.FILL
            paint.color=Color.WHITE
            c.drawCircle(centerX,centerY,radius-30,paint)

            paint.style=Paint.Style.STROKE
            paint.strokeWidth=2f
            paint.color=Color.rgb(225,225,230)
            c.drawCircle(centerX,centerY,radius-30,paint)

            // hour ticks and labels
            for(i in 0 until 24){
                val a=Math.toRadians(i*15.0-90)
                val outer=radius-50
                val inner=if(i%3==0) radius-66 else radius-58
                paint.strokeWidth=if(i%3==0) 4f else 2f
                paint.color=Color.rgb(80,80,88)
                c.drawLine(
                    centerX+cos(a)*inner, centerY+sin(a)*inner,
                    centerX+cos(a)*outer, centerY+sin(a)*outer, paint
                )
            }
            textPaint.textAlign=Paint.Align.CENTER
            textPaint.textSize=17f
            textPaint.color=Color.rgb(55,55,62)
            for(i in hourLabels.indices){
                val a=Math.toRadians(hourPositions[i]*15.0-90)
                val rr=radius-82
                c.drawText(hourLabels[i],centerX+cos(a)*rr,centerY+sin(a)*rr+6,textPaint)
            }

            // current time hand
            val cal=Calendar.getInstance()
            val hour=cal.get(Calendar.HOUR_OF_DAY)+cal.get(Calendar.MINUTE)/60f+cal.get(Calendar.SECOND)/3600f
            val a=Math.toRadians(hour/24*360-90)
            paint.color=Color.rgb(55,55,65); paint.strokeWidth=5f; paint.strokeCap=Paint.Cap.ROUND
            c.drawLine(centerX,centerY,centerX+cos(a)*(radius-88),centerY+sin(a)*(radius-88),paint)
            paint.style=Paint.Style.FILL
            paint.color=Color.rgb(91,91,214)
            c.drawCircle(centerX,centerY,9f,paint)

            // center info
            textPaint.textSize=26f
            textPaint.color=Color.rgb(35,35,42)
            c.drawText("Heute",centerX,centerY+42,textPaint)
            textPaint.textSize=14f
            textPaint.color=Color.rgb(110,110,120)
            c.drawText("Tippen zum Hinzufügen",centerX,centerY+66,textPaint)

            // title
            textPaint.textSize=27f; textPaint.color=Color.rgb(30,30,36)
            c.drawText("DayClock",centerX,55f,textPaint)
            textPaint.textSize=15f; textPaint.color=Color.rgb(105,105,115)
            c.drawText("24-Stunden Tagesplan",centerX,80f,textPaint)
        }

        private fun cos(a:Double)=kotlin.math.cos(a).toFloat()
        private fun sin(a:Double)=kotlin.math.sin(a).toFloat()

        private fun addEventDialog(){
            val box=LinearLayout(this@MainActivity).apply{
                orientation=LinearLayout.VERTICAL
                setPadding(48,10,48,0)
            }
            val title=EditText(this@MainActivity).apply{hint="Aktivität, z. B. Lernen"}
            val start=EditText(this@MainActivity).apply{hint="Startzeit (z. B. 15:00)"}
            val end=EditText(this@MainActivity).apply{hint="Endzeit (z. B. 17:00)"}
            box.addView(title); box.addView(start); box.addView(end)
            AlertDialog.Builder(this@MainActivity)
                .setTitle("Zeitblock hinzufügen")
                .setView(box)
                .setNegativeButton("Abbrechen",null)
                .setPositiveButton("Hinzufügen"){_,_-> 
                    val s=parseTime(start.text.toString())
                    val e=parseTime(end.text.toString())
                    if(title.text.isNotBlank() && s!=null && e!=null && e>s){
                        events.add(Event(title.text.toString(),s,e,Color.rgb(91,91,214)))
                        invalidate()
                    }
                }.show()
        }
        private fun parseTime(v:String):Float?=try{
            val p=v.trim().split(":")
            if(p.size!=2) null else p[0].toFloat()+p[1].toFloat()/60f
        }catch(_:Exception){null}
    }
}
