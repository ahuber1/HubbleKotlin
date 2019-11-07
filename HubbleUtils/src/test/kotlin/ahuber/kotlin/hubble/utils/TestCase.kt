package ahuber.kotlin.hubble.utils

import org.assertj.core.api.AbstractObjectAssert
import org.assertj.core.api.Assertions.assertThat

interface Assertable<T> {
    val expectedOutput: T
}

data class TestCase<TInput, TOutput>(val input: TInput, override val expectedOutput: TOutput) : Assertable<TOutput>

data class ValueTestCase<TInput, T, TOutput>(val input: TInput, val value: T, override val expectedOutput: TOutput) :
        Assertable<TOutput>

inline fun <TInput, TOutput> TestCase<TInput, TOutput>.assertThat(
        block: (TInput) -> TOutput): AbstractObjectAssert<*, TOutput> = assertThat(block(input))!!

inline fun <TInput, T, TOutput> ValueTestCase<TInput, T, TOutput>.assertThat(
        block: (input: TInput, value: T) -> TOutput): AbstractObjectAssert<*, TOutput> = assertThat(block(input, value))!!

infix fun <T> AbstractObjectAssert<*, T>.passes(testCase: Assertable<T>) =
        this.isEqualTo(testCase.expectedOutput)!!

infix fun <T> AbstractObjectAssert<*, T>.fails(testCase: Assertable<T>) =
        this.isNotEqualTo(testCase.expectedOutput)!!