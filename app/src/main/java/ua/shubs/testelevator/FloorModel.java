package ua.shubs.testelevator;

/**
 * Created by Sergbek on 07.03.2016.
 */
public class FloorModel {

    public int number;
    public float begin;
    public float end;

    public FloorModel(int _number, float _begin, float _end) {
        number = _number;
        begin = _begin;
        end = _end;
    }

    strictfp public void change(float _count){
        begin -= _count;
        end -= _count;
    }
}
