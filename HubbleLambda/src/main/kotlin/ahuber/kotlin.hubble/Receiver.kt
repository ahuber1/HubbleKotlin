package ahuber.kotlin.hubble

import ahuber.kotlin.hubble.adt.Buffer
import ahuber.kotlin.hubble.adt.SizeObserver
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore

class Receiver(private val buffer: Buffer<Int>, private val processor: Processor<Array<Int>>,
        private val threshold: Int) : SizeObserver<Buffer<Int>>, Runnable {

    private val semaphore = Semaphore(1)

    init {
        require(threshold >= 1) { "The threshold must be greater than or equal to one. Threshold was $threshold" }
    }

    override fun run() {

        // Immediately try to acquire a semaphore. The semaphore will be acquired when enough data has become
        // available in the IntBuffer
        runBlocking { semaphore.acquire() }

        // Release the semaphore so we "clean up after ourselves."
        semaphore.release()

        // Take the first "threshold" values
        val values = buffer.extract(threshold)
        val array = values.toTypedArray()
        processor.onReceived(array)
    }

    override fun sizeChanged(collection: Buffer<Int>) {
        if (collection.size >= threshold) {
            // Release the semaphore that was acquired in the constructor, thereby letting the run()
            // method continue in its execution so it can alert the receiver.
            semaphore.release()
        }
    }
}