package cn.com.kunghsu.desktop.system;

/**
 */
public class Pointer
{
    public int x;
    public int y;
    public int style;

    @Override
    public boolean equals(Object obj)
    {
        if (null == obj) return false;
        if (!(obj instanceof Pointer)) return false;
        else
        {
            Pointer p = (Pointer) obj;
            return p.x == this.x && p.y == this.y && p.style == this.style;
        }
    }
}
