package ahuber.kotlin.hubble.utils

import jdk.nashorn.internal.ir.annotations.Ignore
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

typealias StringWrapper = ArrayCollectionWrapper<String>
typealias StringCollection = Collection<String>
typealias StringSequence = Sequence<String>
typealias StringArray = Array<String>

internal class ArrayCollectionWrapperTest {

    @ParameterizedTest
    @MethodSource // TODO
    fun testGetSize(testCase: TestCase<StringWrapper, Int>) {
        testCase.assertThat { it.size } passes testCase
    }

    @ParameterizedTest
    @MethodSource // TODO
    fun testContains(testCase: ValueTestCase<StringWrapper, String, Boolean>) {
        testCase.assertThat { wrapper, string -> string in wrapper } passes testCase
    }

    @ParameterizedTest
    @MethodSource // TODO
    fun testContainsAll(testCase: ValueTestCase<StringWrapper, StringCollection, Boolean>) {
        testCase.assertThat { wrapper, collection -> wrapper.containsAll(collection) } passes testCase
    }

    @ParameterizedTest
    @MethodSource // TODO
    fun testGet(testCase: ValueTestCase<StringWrapper, Int, String>) {
        testCase.assertThat { wrapper, index -> wrapper[index] } passes testCase
    }

    @ParameterizedTest
    @MethodSource // TODO
    fun testIndexOf(testCase: ValueTestCase<StringWrapper, String, Int>) {
        testCase.assertThat { wrapper, string -> wrapper.indexOf(string) } passes testCase
    }

    @ParameterizedTest
    @MethodSource // TODO
    fun testIsEmpty(testCase: TestCase<StringWrapper, Boolean>) {
        testCase.assertThat { it.isEmpty() } passes testCase
    }

    @Test
    @Ignore
    fun testIterator() {
        TODO("Not implemented")
    }

    @ParameterizedTest
    @MethodSource // TODO
    fun testLastIndexOf(testCase: ValueTestCase<StringWrapper, String, Int>) {
        testCase.assertThat { wrapper, string -> wrapper.lastIndexOf(string) } passes testCase
    }

    @ParameterizedTest
    @MethodSource // TODO
    fun testAdd(wrapper: StringWrapper) {
        assertThrows<UnsupportedOperationException> {
            wrapper.add(wrapper.indices.middle, "This will never be added")
        }

        assertThrows<UnsupportedOperationException> {
            wrapper.add("This too will never be added.")
        }
    }

    @ParameterizedTest
    @MethodSource // TODO
    fun testAddAll(input: Pair<StringWrapper, StringCollection>) {
        assertThrows<UnsupportedOperationException> {
            input.first.addAll(input.second)
        }
    }

    @ParameterizedTest
    @MethodSource // TODO
    fun testClear(wrapper: StringWrapper) {
        assertThrows<UnsupportedOperationException>(wrapper::clear)
    }

    @Test
    @Ignore
    fun testListIterator() {
        TODO("Not implemented")
    }

    @ParameterizedTest
    @MethodSource // TODO
    fun testRemove(input: Pair<StringWrapper, String>) {
        assertThrows<UnsupportedOperationException> { input.first.remove(input.second) }
    }

    @ParameterizedTest
    @MethodSource // TODO
    fun testRemoveAll(input: Pair<StringWrapper, StringCollection>) {
        assertThrows<UnsupportedOperationException> { input.first.removeAll(input.second) }
    }

    @ParameterizedTest
    @MethodSource // TODO
    fun testRemoveAt(input: Pair<StringWrapper, Int>) {
        assertThrows<UnsupportedOperationException> { input.first.removeAt(input.second) }
    }

    @ParameterizedTest
    @MethodSource // TODO
    fun testRetainAll(input: Pair<StringWrapper, StringCollection>) {
        assertThrows<UnsupportedOperationException> { input.first.retainAll(input.second) }
    }

    @ParameterizedTest
    @MethodSource // TODO
    fun testSet(testCase: ValueTestCase<StringWrapper, IndexedValue<String>, String>) {
        testCase.assertThat { wrapper, (index, string) -> wrapper.set(index, string) } passes testCase
    }

    @ParameterizedTest
    @MethodSource // TODO
    fun testSubList(testCase: ValueTestCase<StringWrapper, IntRange, StringSequence>) {
        testCase.assertThat { wrapper, range -> wrapper.subList(range.first, range.last - 1).asSequence() } passes testCase
    }

    @ParameterizedTest
    @MethodSource // TODO
    fun testGetArray(testCase: TestCase<StringWrapper, StringArray>) {
        testCase.assertThat { it.array } passes testCase
    }
}