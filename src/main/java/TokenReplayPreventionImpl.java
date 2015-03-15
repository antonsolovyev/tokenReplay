import java.util.*;
import java.util.concurrent.*;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;


/**
 * A simple implementation of token tracker using a concurrent hash map.
 */
public class TokenReplayPreventionImpl implements TokenReplayPrevention, InitializingBean, DisposableBean
{
    private static final Logger LOGGER = Logger.getLogger(TokenReplayPreventionImpl.class.getName());

    private ConcurrentMap<String, Token> tokens = new ConcurrentHashMap<String, Token>();
    private final TimeUnit cleanupPeriodTimeUnit;
    private final long cleanupPeriod;
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    public TokenReplayPreventionImpl(TimeUnit cleanupPeriodTimeUnit, long cleanupPeriod)
    {
        this.cleanupPeriodTimeUnit = cleanupPeriodTimeUnit;
        this.cleanupPeriod = cleanupPeriod;

        tokens = new ConcurrentHashMap<String, Token>();
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    }

    public boolean isTokenReplayed(Token token)
    {
        if (!isCurrent(token))
        {
            throw new IllegalArgumentException("Expired token passed");
        }

        Token found = tokens.get(token.getTokenID());
        if ((found != null) && !isCurrent(found))
        {
            tokens.remove(token.getTokenID());
        }

        found = tokens.putIfAbsent(token.getTokenID(), token);
        if (found != null)
        {
            return true;
        }

        return false;
    }

    public int getTokenMapSize()
    {
        return tokens.size();
    }

    private void cleanupExpiredTokens()
    {
        LOGGER.debug("Cleaning up expired tokens, map size before cleanup: " + tokens.size());

        for (Iterator<Map.Entry<String, Token>> i = tokens.entrySet().iterator(); i.hasNext();)
        {
            Map.Entry<String, Token> e = i.next();

            if (!isCurrent(e.getValue()))
            {
                i.remove();
            }
        }

        LOGGER.debug("Map size after cleanup: " + tokens.size());
    }

    private boolean isCurrent(Token t)
    {
        Date now = new Date();

        if (now.after(t.getNotValidBefore()) && now.before(t.getNotValidAfter()))
        {
            return true;
        }

        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        scheduledThreadPoolExecutor.scheduleAtFixedRate(new Runnable()
            {
                @Override
                public void run()
                {
                    cleanupExpiredTokens();
                }
            }, 0, cleanupPeriod, cleanupPeriodTimeUnit);
    }

    @Override
    public void destroy() throws Exception
    {
        scheduledThreadPoolExecutor.shutdownNow();
    }
}
