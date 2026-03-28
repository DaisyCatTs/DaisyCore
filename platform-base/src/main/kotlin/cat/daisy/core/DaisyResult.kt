package cat.daisy.core

public sealed interface DaisyResult<out T> {
    public data class Success<T>(
        val value: T,
    ) : DaisyResult<T>

    public data class Failure(
        val message: String,
        val cause: Throwable? = null,
    ) : DaisyResult<Nothing>
}
