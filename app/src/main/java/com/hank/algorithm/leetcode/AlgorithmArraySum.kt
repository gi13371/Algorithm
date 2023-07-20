package com.hank.algorithm.leetcode

/**
 * Author: hank.liu
 * Date: 2023/7/17 14:37
 * Copyright: 2023 www.xgimi.com Inc. All rights reserved.
 * Desc: 给定一个数组和一个目标和，从数组中找两个数字相加等于目标和，输出这两个数字的下标。
 */
object AlgorithmArraySum {
    /**
     *  直接暴力解法
     *  分两层遍历取值逐个相加，直到找到满足条件的下标
     *  时间复杂度 O（n²）
     *  空间复杂度 O（1）
     */
    fun twoArraySum(array: IntArray, target: Int): IntArray {
        val ans = intArrayOf(-1, -1)
        for (i in array.indices) {
            for (j in i + 1 until array.size) {
                if (array[i] + array[j] == target) {
                    ans[0] = i
                    ans[1] = j
                    return ans
                }
            }
        }
        return ans
    }

    /**
     *  利用hashMap解法
     *  分两层遍历取值逐个相加，直到找到满足条件的下标
     *  时间复杂度 O（n）
     *  空间复杂度 O（1）
     */
    fun twoArraySumForMap(array: IntArray, target: Int): IntArray {
        val ans = intArrayOf(-1, -1)
        val tempMap = hashMapOf <Int,Int>()

        for (i in array.indices) {
            val sub = target - array[i]
            if (tempMap.containsKey(sub)){
                ans[0] = i
                ans[1] = tempMap[sub]!!
                return ans
            }
            tempMap[array[i]] = i  // 放在最后，确保找到的时候是先找到ans里第二个的下标，不会出现同一个下标
        }
        return ans
    }
}