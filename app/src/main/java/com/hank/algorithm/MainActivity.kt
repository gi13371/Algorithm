package com.hank.algorithm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hank.algorithm.leetcode.AlgorithmArraySum

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 给定一个数组和一个目标和，从数组中找两个数字相加等于目标和，输出这两个数字的下标。
        val indexArray = AlgorithmArraySum.twoArraySumForMap(intArrayOf(1, 2, 3, 4, 5), 4)
        println("result = ${indexArray[0]}, ${indexArray[1]}")
    }
}