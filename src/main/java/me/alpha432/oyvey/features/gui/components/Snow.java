package me.alpha432.oyvey.features.gui.components;

import me.alpha432.oyvey.util.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;

import java.util.Random;

public class Snow
{
    private int _x;
    private int _y;
    private int _fallingSpeed;
    private int _size;

    public Snow(int x, int y, int fallingSpeed, int size)
    {
        _x = x;
        _y = y;
        _fallingSpeed = fallingSpeed;
        _size = size;
    }

    public int getX()
    {
        return _x;
    }

    public void setX(int x)
    {
        this._x = x;
    }

    public int getY()
    {
        return _y;
    }

    public void setY(int _y)
    {
        this._y = _y;
    }

    public void Update(ScaledResolution res)
    {
        RenderUtil.drawRect(getX(), getY(), getX()+_size, getY()+_size, 0x99C9C5C5);

        setY(getY() + _fallingSpeed);

        if (getY() > res.getScaledHeight() + 10 || getY() < -10)
        {
            setY(-10);

            Random rand = new Random();

            _fallingSpeed = rand.nextInt(10) + 1;
            _size = rand.nextInt(4) + 1;
        }
    }
}