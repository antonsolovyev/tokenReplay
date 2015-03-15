public class TokenReplayPreventionFactory
{
    @Deprecated
    static TokenReplayPrevention getInstance()
    {
        throw new IllegalStateException("Please use Spring context to receive an instance of a TokenReplayPrevention");
    }
}
