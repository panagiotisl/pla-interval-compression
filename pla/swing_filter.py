import math
import numpy as np
import pandas


def apply(timeseries, e, init_structure_func, insert_func, quantization_func):
    points = []
    structure = init_structure_func()
    for idx in range(len(timeseries)):
        up_val = timeseries.iloc[idx] + e
        down_val = timeseries.iloc[idx] - e

        if len(points) == 0:
            a_min, a_max = -math.inf, math.inf
            points = [idx]
            continue

        if len(points) >= 2:
            up_lim = a_max * (idx - points[0]) + quantization_func(timeseries.iloc[points[0]], e)
            down_lim = a_min * (idx - points[0]) + quantization_func(timeseries.iloc[points[0]], e)
            if (not np.isclose(down_val, up_lim, atol=10 ** -10) and down_val > up_lim) or (
                    not np.isclose(up_val, down_lim, atol=10 ** -10) and up_val < down_lim):
                insert_func(structure, points[0], a_min, a_max, quantization_func(timeseries.iloc[points[0]], e))
                points = [idx]
                continue

        a_max_temp = (up_val - quantization_func(timeseries.iloc[points[0]], e)) / (idx - points[0])
        a_min_temp = (down_val - quantization_func(timeseries.iloc[points[0]], e)) / (idx - points[0])

        if len(points) == 1:
            a_max = a_max_temp
            a_min = a_min_temp
        else:
            up_lim = a_max * (idx - points[0]) + quantization_func(timeseries.iloc[points[0]], e)
            if not np.isclose(up_val, up_lim, atol=10 ** -10) and up_val < up_lim:
                a_max = a_max_temp if a_max_temp >= a_min else a_min
            down_lim = a_min * (idx - points[0]) + quantization_func(timeseries.iloc[points[0]], e)
            if not np.isclose(down_val, down_lim, atol=10 ** -10) and down_val > down_lim:
                a_min = a_min_temp if a_min_temp <= a_max else a_max

        points.append(idx)

    if len(points) >= 0:
        insert_func(structure, points[0], a_min, a_max, quantization_func(timeseries.iloc[points[0]], e))

    return structure, timeseries.index[-1]


def reconstruct(segments, last_idx):
    x = []
    y = []
    for segment_idx in range(len(segments)):
        if segment_idx + 1 == len(segments):
            t_end = last_idx + 1
        else:
            t_end = segments[segment_idx + 1][0]
        t_start, a_max, a_min, b = \
            segments[segment_idx][0], segments[segment_idx][1], segments[segment_idx][2], segments[segment_idx][3]
        for t in range(t_start, t_end):
            x.append(t)
            y_val = ((a_max + a_min) / 2) * (t - t_start) + b
            y.append(y_val)

    return pandas.Series(y, index=x)


def validate(original, approximation, e):
    cmp = original.compare(approximation, keep_equal=True)
    cmp['valid'] = np.isclose(cmp['self'], cmp['other'], atol=e)
    num_errors = len(cmp) - cmp['valid'].sum()

    if num_errors > 0:
        raise Exception('Found ' + str(num_errors) + ' errors')

    return True
