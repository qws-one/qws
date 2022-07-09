import java.time.Instant

@Suppress("MemberVisibilityCanBePrivate", "unused")
class Info {
    data class Instance(val hashCode: Int, val type: Class) {
        companion object {
            fun of(any: Any) = any::class.let { c -> Instance(any.hashCode(), Class(c.simpleName ?: "", c.qualifiedName ?: "", c.java.canonicalName ?: "")) }
        }
    }

    data class Class(val name: String, val qualifiedName: String, val parentQualifiedName: String)

    data class Thread(val id: Long, val initialName: String, val someTimePoint: TimePoint = TimePoint.empty) {
        companion object {
            fun current(): Thread {
                val curr = java.lang.Thread.currentThread()
                return Thread(curr.id, curr.name, TimePoint.current())
            }
        }
    }

    data class TimePoint(val systemNanoTime: Long, val currentTimeMillis: Long) {
        companion object {
            val empty = TimePoint(-1, -1)
            fun current() = TimePoint(System.nanoTime(), System.currentTimeMillis())
        }
    }

    data class Process(val id: Long, val parentId: Long, val startTimeMillis: Long, val cmd: String, val cmdLine: String, val user: String) {
        companion object {
            val empty = Process(-1, -1, -1, "", "", "")

            val current by lazy { of(ProcessHandle.current()) }

            fun of(pid: Long): Process {
                if (pid > 0) {
                    val processHandle = ProcessHandle.of(pid).orElse(null)
                    if (null != processHandle) return of(processHandle)
                }
                return empty
            }

            fun of(processHandle: ProcessHandle): Process {
                val pid = processHandle.pid()
                val info = processHandle.info()
                val startInstant = info.startInstant().orElse(null)
                if (null != startInstant && startInstant.epochSecond > 0) return Process(
                    pid,
                    processHandle.parent().orElse(null)?.pid() ?: -1,
                    startInstant.toEpochMilli(),
                    info.command().orElse(""),
                    info.commandLine().orElse(""),
                    info.user().orElse("")
                )
                return empty
            }

            fun cpuNanoTime(pid: Long): Long {
                if (pid > 0) {
                    val processHandle = ProcessHandle.of(pid).orElse(null)
                    if (null != processHandle) return cpuNanoTime(processHandle)
                }
                return -1
            }

            fun cpuNanoTime(processHandle: ProcessHandle): Long {
                val info = processHandle.info()
                val totalCpuDuration = info.totalCpuDuration().orElse(null)
                return if (null != totalCpuDuration) return totalCpuDuration.toNanos() else -1
            }
        }
    }
}