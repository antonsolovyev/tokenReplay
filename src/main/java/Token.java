
import java.util.Arrays;
import java.util.Date;


/**
 * A class representing a security token.
 */
public class Token
{
    private String tokenID;
    private Date notValidBefore;
    private Date notValidAfter;
    private TokenSignature tokenSignature;
    private byte[] rawToken;

    public Token(String tokenID, Date notValidBefore, Date notValidAfter, TokenSignature tokenSignature,
        byte[] rawToken)
    {
        this.tokenID = tokenID;
        this.notValidBefore = notValidBefore;
        this.notValidAfter = notValidAfter;
        this.tokenSignature = tokenSignature;
        this.rawToken = rawToken;
    }

    /**
     *
     * Get the unique identifier for this token (guaranteed to be unique across all tokens).
     * @return the token ID
     */
    public String getTokenID()
    {
        return tokenID;
    }

    /**
     * Get the beginning of the token validity window.
     * @return the date before which this token is not valid
     */
    public Date getNotValidBefore()
    {
        return notValidBefore;
    }

    /**
     * Get the end of the token validity window.
     * @return the date after which this token is not valid
     */
    public Date getNotValidAfter()
    {
        return notValidAfter;
    }

    /**
     * Get the object that contains signature information for this token.
     * @return this tokens signature
     */
    public TokenSignature getTokenSignature()
    {
        return tokenSignature;
    }

    /**
     * Get the binary content of the token.
     * @return a byte array containing all the token data encoded in binary.
     */
    public byte[] getRawToken()
    {
        return rawToken;
    }

    /**
     * Tokens can be compared using token IDs. Boilerplate generated using IDE.
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        Token token = (Token) o;

        if ((tokenID != null) ? (!tokenID.equals(token.tokenID)) : (token.tokenID != null))
        {
            return false;
        }

        return true;
    }

    /**
     * Hashcode boilerplate, generated using IDE.
     *
     * @return
     */
    @Override
    public int hashCode()
    {
        return (tokenID != null) ? tokenID.hashCode() : 0;
    }

    @Override
    public String toString()
    {
        return "Token{" +
            "tokenID='" + tokenID + '\'' +
            ", notValidBefore=" + notValidBefore +
            ", notValidAfter=" + notValidAfter +
            ", tokenSignature=" + tokenSignature +
            ", rawToken=" + Arrays.toString(rawToken) +
            '}';
    }
}