package ahuber.kotlin.hubble

interface Processor<T> {
    /**
     * A method that is invoked when the data has become available for processing.
     * @param data The data
     */
    fun onReceived(data: T)
}