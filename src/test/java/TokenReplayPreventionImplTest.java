import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class TokenReplayPreventionImplTest
{
    private static final Logger LOGGER = Logger.getLogger(TokenReplayPreventionImplTest.class.getName());

    /**
     * Test a single token, basic sanity, NPEs, etc.
     *
     * @throws Exception
     */
    @Test
    public void testSingle() throws Exception
    {
        TokenReplayPrevention tokenReplayPrevention = new TokenReplayPreventionImpl(TimeUnit.SECONDS, 1);

        Token token = makeToken("dummy-token-ID-1", Integer.MIN_VALUE, Integer.MAX_VALUE);

        assertTrue("token should have been found unique", !tokenReplayPrevention.isTokenReplayed(token));
    }

    /**
     * Feed all unique tokens.
     *
     * @throws Exception
     */
    @Test
    public void testUniqueTokens() throws Exception
    {
        Token token = makeToken("dummy-token-ID-1", Integer.MIN_VALUE, Integer.MAX_VALUE);
        Token token2 = makeToken("dummy-token-ID-2", Integer.MIN_VALUE, Integer.MAX_VALUE);
        Token token3 = makeToken("dummy-token-ID-3", Integer.MIN_VALUE, Integer.MAX_VALUE);

        TokenReplayPrevention tokenReplayPrevention = new TokenReplayPreventionImpl(TimeUnit.SECONDS, 1);

        assertFalse("token should have been found unique", tokenReplayPrevention.isTokenReplayed(token));
        assertFalse("token should have been found unique", tokenReplayPrevention.isTokenReplayed(token2));
        assertFalse("token should have been found unique", tokenReplayPrevention.isTokenReplayed(token3));
    }

    /**
     * Feed unique and then repeat tokens.
     *
     * @throws Exception
     */
    @Test
    public void testDuplicateTokens() throws Exception
    {
        Token token = makeToken("dummy-token-ID-1", Integer.MIN_VALUE, Integer.MAX_VALUE);
        Token token2 = makeToken("dummy-token-ID-2", Integer.MIN_VALUE, Integer.MAX_VALUE);
        Token token3 = makeToken("dummy-token-ID-3", Integer.MIN_VALUE, Integer.MAX_VALUE);
        Token token4 = makeToken("dummy-token-ID-1", Integer.MIN_VALUE, Integer.MAX_VALUE);

        TokenReplayPrevention tokenReplayPrevention = new TokenReplayPreventionImpl(TimeUnit.SECONDS, 1);

        assertFalse("token should have been found unique", tokenReplayPrevention.isTokenReplayed(token));
        assertFalse("token should have been found unique", tokenReplayPrevention.isTokenReplayed(token2));
        assertFalse("token should have been found unique", tokenReplayPrevention.isTokenReplayed(token3));

        assertTrue("token should have been found already seen", tokenReplayPrevention.isTokenReplayed(token));
        assertTrue("token should have been found already seen", tokenReplayPrevention.isTokenReplayed(token4));
    }

    /**
     * Make sure we don't mind replaying expired tokens.
     *
     * @throws Exception
     */
    @Test
    public void testIgnoreExpiredTokens() throws Exception
    {
        Token token = makeToken("dummy-token-ID-1", Integer.MIN_VALUE, 1);
        Token token2 = makeToken("dummy-token-ID-1", -20, 60);

        TokenReplayPrevention tokenReplayPrevention = new TokenReplayPreventionImpl(TimeUnit.SECONDS, 1);

        assertFalse("token should have been found unique", tokenReplayPrevention.isTokenReplayed(token));

        Thread.sleep(TimeUnit.SECONDS.toMillis(2));

        assertFalse("token should have been found unique", tokenReplayPrevention.isTokenReplayed(token2));
    }

    /**
     * Make sure expired tokens get removed. NB: tests the specific implementation detals.
     * You may want to expose some of these parameters via JMX, for instance.
     *
     * @throws Exception
     */
    @Test
    // @Ignore
    public void testCleanup() throws Exception
    {
        // 3 tokens with a 1 sec expiration
        Token token = makeToken("dummy-token-ID-1", Integer.MIN_VALUE, 1);
        Token token2 = makeToken("dummy-token-ID-2", Integer.MIN_VALUE, 1);
        Token token3 = makeToken("dummy-token-ID-3", Integer.MIN_VALUE, 1);
        // One long lived token
        Token token4 = makeToken("dummy-token-ID-1", Integer.MIN_VALUE, Integer.MAX_VALUE);

        TokenReplayPreventionImpl tokenReplayPreventionImpl = new TokenReplayPreventionImpl(TimeUnit.MILLISECONDS, 1);

        assertFalse("token should have been found unique", tokenReplayPreventionImpl.isTokenReplayed(token));
        assertFalse("token should have been found unique", tokenReplayPreventionImpl.isTokenReplayed(token2));
        assertFalse("token should have been found unique", tokenReplayPreventionImpl.isTokenReplayed(token3));

        // Sleep 1 sec, to allow expiration
        Thread.sleep(TimeUnit.SECONDS.toMillis(2));

        assertFalse("token should have been found unique", tokenReplayPreventionImpl.isTokenReplayed(token4));

        // Token map size should be 3 now (two old ones and one current)
        assertEquals("token map size mismatch", 3, tokenReplayPreventionImpl.getTokenMapSize());

        tokenReplayPreventionImpl.afterPropertiesSet();
        Thread.sleep(10);

        // Should be only one left after cleanup
        assertEquals("token map size mismatch", 1, tokenReplayPreventionImpl.getTokenMapSize());

        tokenReplayPreventionImpl.destroy();
    }

    private Token makeToken(String tokenID, int notBeforeSeconds, int notAfterSeconds)
    {
        // Some validity dates on the token
        Calendar notBefore = Calendar.getInstance();
        notBefore.add(Calendar.SECOND, notBeforeSeconds);

        Calendar notAfter = Calendar.getInstance();
        notAfter.add(Calendar.SECOND, notAfterSeconds);

        // For testing, just convert the tokenID to bytes for the raw token value.  A real token might have more stuff
        // but this is sufficient for testing the replay prevention
        byte[] rawToken = tokenID.getBytes();

        // This TokenReplayPrevention class shouldn't even look at the signature so we'll just leave it null
        TokenSignature tokenSignature = null;

        // Create a test Token
        return new Token(tokenID, notBefore.getTime(), notAfter.getTime(), tokenSignature, rawToken);
    }
}
