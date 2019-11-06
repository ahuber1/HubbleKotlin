package ahuber.kotlin.hubble

import ahuber.kotlin.hubble.adt.Buffer
import ahuber.kotlin.hubble.adt.SizeObserver
import java.util.*
import java.util.concurrent.Semaphore

class Satellite(private val buffer: Buffer<Int>) : SizeObserver<Buffer<Int>>, Runnable {
    private val semaphore = Semaphore(1)
    private val random = Random()

    override fun sizeChanged(collection: Buffer<Int>) {
        // Release the semaphore so we can alert the run() method that space may be available in the buffer to
        // add more items.
        semaphore.release()
    }

    override fun run() {
        while (true) {
            val number = random.nextInt()
            var successful: Boolean

            do {
                successful = buffer.add(number)

                if (successful) {
                    continue
                }

                // If we were not successful, wait until the size of the collection changes and try again.
                // We wait by acquiring a semaphore a first time and then a second time. The semaphore is released
                // in sizeChanged(IntBuffer), which enables the semaphore to be acquired that second time.
                semaphore.acquire()
                semaphore.acquire()

                // Now that we have the semaphore, release it so we "clean up after ourselves," and try again.
                semaphore.release()
            } while(!successful)
        }
    }
}