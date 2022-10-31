package ir.types;

import java.util.ArrayList;

public class ArrayType extends ValueType
{
    /**
     * IntType | ArrayType
     */
    private final ValueType elementType;
    /**
     * eg int a[2][3][4]
     * elementNum = 2
     */
    private final int elementNum;
    private final int size;

    /**
     * @param elementType 数组元素类型，可以为数组或者基本类型
     * @param elementNum  数组元素数量
     */
    public ArrayType(ValueType elementType, int elementNum)
    {
        this.elementType = elementType;
        this.elementNum = elementNum;
        this.size = elementType.getSize() * elementNum;
    }

    /**
     * @param dataType   数组的基本数据类型
     * @param dimension 数组各个维度
     *                   eg：int a[1][2][3]
     *                   dataType为 IntType
     *                   dimensions数组为{1，2，3}
     *                   最终的结果为elementType = int [2][3]
     *                   elementNum = 1
     */
    public ArrayType(DataType dataType, ArrayList<Integer> dimension)
    {
        // 这个操作是为了防止这个构造器修改参数
        ArrayList<Integer> dimensions = new ArrayList<>(dimension);
        this.elementNum = dimensions.get(0);
        if (dimensions.size() == 1)
        {
            this.elementType = dataType;
        }
        else
        {
            dimensions.remove(0);
            this.elementType = new ArrayType(dataType, dimensions);
        }
        this.size = elementType.getSize() * elementNum;
    }

    /**
     * @return eg a[2][3][4] 返回2
     */
    public int getElementNum()
    {
        return elementNum;
    }

    public ValueType getElementType()
    {
        return elementType;
    }

    /**
     * @return 数组基本元素的类型，只能为IntType
     */
    public DataType getDataType()
    {
        ValueType type = elementType;
        while (type instanceof ArrayType)
        {
            type = ((ArrayType) type).getElementType();
        }
        return (DataType) type;
    }

    @Override
    public int getSize()
    {
        return size;
    }

    /**
     * @return 数组的元素总数，例如int a[1][2][3][4] 返回的元素总数为 1 * 2 * 3 * 4 = 24
     */
    public int getTotalElementNum()
    {
        return getSize() / 4;
    }

    /**
     * @return 数组的维度信息，例如int a[1][2][3][4]返回的dims数组为{1，2，3，4}
     */
    public ArrayList<Integer> getDims()
    {
        ArrayList<Integer> dims = new ArrayList<>();
        dims.add(elementNum);
        ValueType tmpType = elementType;
        while (tmpType instanceof ArrayType)
        {
            dims.add(((ArrayType) tmpType).getElementNum());
            tmpType = ((ArrayType) tmpType).getElementType();
        }
        return dims;
    }

    @Override
    public String toString()
    {
        return "[" + elementNum + " x " + elementType + "]";
    }
}

