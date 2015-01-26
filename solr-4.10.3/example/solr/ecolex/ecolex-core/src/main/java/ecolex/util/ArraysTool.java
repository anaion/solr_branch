//
// Copyright (c) 2007, Warsaw University of Technology.
// All rights reserved.
//

package ecolex.util;

import java.util.Arrays;
import java.util.List;

public class ArraysTool
{
    public static <T> List<T> asList(T[] a) {
        return Arrays.asList(a);
    }
}