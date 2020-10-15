package com.solvindskycams.common.mapping

interface Mapper<I, O> {
    fun map(input: I): O
}