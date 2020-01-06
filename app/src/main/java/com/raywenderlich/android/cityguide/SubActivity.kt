package com.raywenderlich.android.cityguide

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_sub.*

class SubActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub)

        btn2.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        calendarView?.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val msg:String = year.toString() + month.toString() + dayOfMonth.toString()
            runOnUiThread {
                Toast.makeText(this,msg,Toast.LENGTH_SHORT).show() //날짜 터치시 알림표시로 나타내기
            }

//textView.text = "$msg"
        }

    }
}