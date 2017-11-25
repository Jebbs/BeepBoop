public class SuperBlock
{
    public int totalBlocks;
    public int totalInodes;
    private int freeList[];

    public SuperBlock(int diskSize)
    {
        byte[] block = new byte[Disk.blockSize];
        SysLib.rawread(0, block);

        totalBlocks = SysLib.bytes2int(block, 0);
        totalInodes = SysLib.bytes2int(block, 4);

        freeList = new int[32];
        for(int i = 0; i < 32; ++i)
            freeList[i] = SysLib.bytes2int(block, 8 + 4 * i);
    }

    public boolean getFreeList(short i)
    {
        if(i < 0 || i >= 1000)
            return false;

        int x = 1 << (i & 31);
        return (freeList[i / 32] & x) == x;
    }

    public void setFreeList(short i, boolean b)
    {
        if(i < 0 || i >= 1000)
            return;

        int x = 1 << (i & 31);

        if(b)
            freeList[i / 32] |= x;
        else
            freeList[i / 32] &= ~x;
    }

    public void clearFreeList()
    {
        for(short i = 0; i < 32; ++i)
            freeList[i] = 0;

        setFreeList((short)0, true);
    }

    public short findFirstFreeBlock()
    {
        for(short i = 0; i < 32; ++i) {
            if(freeList[i] != 0xFFFFFFFF) {
                short j = (short)(i << 5);
                for(short k = j; k < (j>968?1000:j+32); ++k)
                    if(!getFreeList(k))
                        return k;
            }
        }

        return -1;
    }

    public void sync()
    {
        byte[] block = new byte[Disk.blockSize];
        SysLib.int2bytes(totalBlocks, block, 0);
        SysLib.int2bytes(totalInodes, block, 4);

        for(short i = 0; i < 32; ++i)
            SysLib.int2bytes(freeList[i], block, 8 + 4 * i);

        SysLib.rawwrite(0, block);
    }
}
