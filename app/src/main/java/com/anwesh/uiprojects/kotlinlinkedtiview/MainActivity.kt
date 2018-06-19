package com.anwesh.uiprojects.kotlinlinkedtiview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.linkedtiview.LinkedTIView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LinkedTIView.create(this)
    }
}
