package net.minecraft.src;

public class LongHashMap
{
    private transient LongHashMapEntry hashArray[];
    private transient int numHashElements;
    private int capacity;
    private final float percent = 0.75F;
    private volatile transient int modCount;

    public LongHashMap()
    {
        capacity = 12;
        hashArray = new LongHashMapEntry[16];
    }

    private static int getHashedKey(long l)
    {
        return hash((int)(l ^ l >>> 32));
    }

    private static int hash(int i)
    {
        i ^= i >>> 20 ^ i >>> 12;
        return i ^ i >>> 7 ^ i >>> 4;
    }

    private static int getHashIndex(int i, int j)
    {
        return i & j - 1;
    }

    public int getNumHashElements()
    {
        return numHashElements;
    }

    public Object getValueByKey(long l)
    {
        int i = getHashedKey(l);
        for (LongHashMapEntry longhashmapentry = hashArray[getHashIndex(i, hashArray.length)]; longhashmapentry != null; longhashmapentry = longhashmapentry.nextEntry)
        {
            if (longhashmapentry.key == l)
            {
                return longhashmapentry.value;
            }
        }

        return null;
    }

    public boolean containsKey(long l)
    {
        return getEntry(l) != null;
    }

    final LongHashMapEntry getEntry(long l)
    {
        int i = getHashedKey(l);
        for (LongHashMapEntry longhashmapentry = hashArray[getHashIndex(i, hashArray.length)]; longhashmapentry != null; longhashmapentry = longhashmapentry.nextEntry)
        {
            if (longhashmapentry.key == l)
            {
                return longhashmapentry;
            }
        }

        return null;
    }

    public void add(long l, Object obj)
    {
        int i = getHashedKey(l);
        int j = getHashIndex(i, hashArray.length);
        for (LongHashMapEntry longhashmapentry = hashArray[j]; longhashmapentry != null; longhashmapentry = longhashmapentry.nextEntry)
        {
            if (longhashmapentry.key == l)
            {
                longhashmapentry.value = obj;
            }
        }

        modCount++;
        createKey(i, l, obj, j);
    }

    private void resizeTable(int i)
    {
        LongHashMapEntry alonghashmapentry[] = hashArray;
        int j = alonghashmapentry.length;
        if (j == 0x40000000)
        {
            capacity = 0x7fffffff;
            return;
        }
        else
        {
            LongHashMapEntry alonghashmapentry1[] = new LongHashMapEntry[i];
            copyHashTableTo(alonghashmapentry1);
            hashArray = alonghashmapentry1;
            capacity = (int)((float)i * percent);
            return;
        }
    }

    private void copyHashTableTo(LongHashMapEntry alonghashmapentry[])
    {
        LongHashMapEntry alonghashmapentry1[] = hashArray;
        int i = alonghashmapentry.length;
        for (int j = 0; j < alonghashmapentry1.length; j++)
        {
            LongHashMapEntry longhashmapentry = alonghashmapentry1[j];
            if (longhashmapentry == null)
            {
                continue;
            }
            alonghashmapentry1[j] = null;
            do
            {
                LongHashMapEntry longhashmapentry1 = longhashmapentry.nextEntry;
                int k = getHashIndex(longhashmapentry.field_35831_d, i);
                longhashmapentry.nextEntry = alonghashmapentry[k];
                alonghashmapentry[k] = longhashmapentry;
                longhashmapentry = longhashmapentry1;
            }
            while (longhashmapentry != null);
        }
    }

    public Object remove(long l)
    {
        LongHashMapEntry longhashmapentry = removeKey(l);
        return longhashmapentry != null ? longhashmapentry.value : null;
    }

    final LongHashMapEntry removeKey(long l)
    {
        int i = getHashedKey(l);
        int j = getHashIndex(i, hashArray.length);
        LongHashMapEntry longhashmapentry = hashArray[j];
        LongHashMapEntry longhashmapentry1;
        LongHashMapEntry longhashmapentry2;
        for (longhashmapentry1 = longhashmapentry; longhashmapentry1 != null; longhashmapentry1 = longhashmapentry2)
        {
            longhashmapentry2 = longhashmapentry1.nextEntry;
            if (longhashmapentry1.key == l)
            {
                modCount++;
                numHashElements--;
                if (longhashmapentry == longhashmapentry1)
                {
                    hashArray[j] = longhashmapentry2;
                }
                else
                {
                    longhashmapentry.nextEntry = longhashmapentry2;
                }
                return longhashmapentry1;
            }
            longhashmapentry = longhashmapentry1;
        }

        return longhashmapentry1;
    }

    private void createKey(int i, long l, Object obj, int j)
    {
        LongHashMapEntry longhashmapentry = hashArray[j];
        hashArray[j] = new LongHashMapEntry(i, l, obj, longhashmapentry);
        if (numHashElements++ >= capacity)
        {
            resizeTable(2 * hashArray.length);
        }
    }

    static int getHashCode(long l)
    {
        return getHashedKey(l);
    }
    
    /* World Downloader >>> */
    public LongHashMapEntry[] getEntries()
    {
    	return hashArray;
    }
    /* <<< World Downloader */
}
