package delta.timewarp.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class TimewarpServerApplication

fun main(args: Array<String>) {
    runApplication<TimewarpServerApplication>(*args)
}
