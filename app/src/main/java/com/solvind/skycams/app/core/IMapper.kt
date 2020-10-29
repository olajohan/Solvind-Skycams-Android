package com.solvind.skycams.app.core

interface IMapper<Left, Right> {
    fun listFromLeftToRight(left: List<Left>): List<Right> = left.map { singleFromLeftToRight(it) }
    fun listFromRightToLeft(right: List<Right>): List<Left> = right.map { singleFromRightToLeft(it) }
    fun singleFromLeftToRight(left: Left) : Right
    fun singleFromRightToLeft(right: Right) : Left
}