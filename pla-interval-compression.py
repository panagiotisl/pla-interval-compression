import csv
import pandas as pd

from os.path import exists
from intervaltree import Interval, IntervalTree
from pla import swing_filter


def quantization(b, e):
    return int(b / e) * e


def init_structure_offline():
    return []


def insert_offline(structure, t_start, a_min, a_max, b):
    structure.append([t_start, a_min, a_max, b])


def init_structure_online():
    return {}


def merge_online(structure, t_start, a_min, a_max, b):
    try:
        tree = structure[b]
    except KeyError:
        structure[b] = IntervalTree()
        merge_online(structure, t_start, a_min, a_max, b)
        return

    overlaps = tree.overlap(a_min, a_max)
    if len(overlaps) == 0:
        tree.add(Interval(a_min, a_max, {t_start}))
    else:
        overlap_interval = overlaps.pop()
        tree.remove(overlap_interval)
        tree.add(
            Interval(
                max(a_min, overlap_interval.begin),
                min(a_max, overlap_interval.end),
                {t_start}.union(overlap_interval.data)
            )
        )


# def merge_offline(segments):
#     segments_merged = []
#     segments.sort(key=lambda x: x[1])
#
#     merged_seg = [{segments[0][3]: [segments[0][0]]}, segments[0][1], segments[0][2]]
#     for i in range(1, len(segments)):
#         if segments[i][1] <= merged_seg[2] and segments[i][2] >= merged_seg[1]:
#             try:
#                 merged_seg[0][segments[i][3]].append(segments[i][0])
#             except KeyError:
#                 merged_seg[0][segments[i][3]] = [segments[i][0]]
#             merged_seg[1] = max(segments[i][1], merged_seg[1])
#             merged_seg[2] = min(segments[i][2], merged_seg[2])
#         else:
#             segments_merged.append(merged_seg)
#             merged_seg = [{segments[i][3]: [segments[i][0]]}, segments[i][1], segments[i][2]]
#     segments_merged.append(merged_seg)
#
#     return segments_merged


def merge_offline(segments):
    segments_merged = []
    segments.sort(key=lambda x: x[1])

    segments_per_b = {}
    for segment in segments:
        try:
            segments_per_b[segment[3]].append(segment)
        except KeyError:
            segments_per_b[segment[3]] = [segment]

    for b, seg in segments_per_b.items():
        merged_seg = [{seg[0][0]}, seg[0][1], seg[0][2], seg[0][3]]
        for i in range(1, len(seg)):
            if seg[i][1] <= merged_seg[2] and seg[i][2] >= merged_seg[1]:
                merged_seg[0].add(seg[i][0])
                merged_seg[1] = max(seg[i][1], merged_seg[1])
                merged_seg[2] = min(seg[i][2], merged_seg[2])
            else:
                segments_merged.append(merged_seg)
                merged_seg = [{seg[i][0]}, seg[i][1], seg[i][2], seg[i][3]]
        segments_merged.append(merged_seg)

    return segments_merged, len(segments_per_b)



def tree_per_b_to_array_of_segments(tree_per_b):
    all_segments = []

    for b, tree in tree_per_b.items():
        for interval in tree:
            for t_start in interval.data:
                all_segments.append([t_start, interval.begin, interval.end, b])
    all_segments.sort(key=lambda x: x[0])

    return all_segments


# def unpack_merged(segments_merged):
#     segments = []
#     for segment_merged in segments_merged:
#         for b, t_starts in segment_merged[0].items():
#             for t_start in t_starts:
#                 segments.append([t_start, segment_merged[1], segment_merged[2], b])
#
#     segments.sort(key=lambda x: x[0])
#
#     return segments


def unpack_merged(segments_merged):
    segments = []
    for segment_merged in segments_merged:
        for t_start in segment_merged[0]:
            segments.append([t_start, segment_merged[1], segment_merged[2], segment_merged[3]])

    segments.sort(key=lambda x: x[0])

    return segments


def parse_file(file):
    df = pd.read_csv(file)
    ts = pd.Series(df['value'], index=df.index)

    return ts


def calculate_ts_size(length_timeseries):
    # t: 4 bytes (int)
    # value: 8 bytes (float)
    return length_timeseries * (4 + 8)


def calculate_swing_size(num_of_segments):
    # a: 8 bytes (float)
    # b: 8 bytes (float)
    # t start: 4 bytes (int)
    # last t: 4 bytes (int)
    return num_of_segments * (8 + 8 + 4) + 4


# def calculate_merged_swing_size(segments_merged):
#     # qnt(a): 4 bytes (int)
#     # #b: 8 bytes (float)
#     # t start: 4 bytes (int)
#     # last t: 4 bytes (int)
#     b_values = set()
#     num_of_a = 0
#     num_of_t = 0
#     for segment_merged in segments_merged:
#         for b, t_starts in segment_merged[0].items():
#             num_of_a += 1
#             b_values.add(b)
#             num_of_t += len(t_starts)
#     return len(b_values) * 8 + num_of_a * 4 + num_of_t * 4 + 4


def calculate_merged_swing_size(segments_merged):
    # qnt(a): 4 bytes (int)
    # #b: 4 bytes (float)
    # t start: 4 bytes (int)
    # last t: 4 bytes (int)
    b_values = set()
    t_values = 0
    for segment_merged in segments_merged:
        t_values += len(segment_merged[0])
        b_values.add(segment_merged[3])
    return len(segments_merged) * 4 + len(b_values) * 4 + t_values * 4 + 4


def write_rows(filename, header, rows):
    with open(filename, 'w', newline='') as csvfile:
        csvwriter = csv.writer(csvfile)
        csvwriter.writerow(header)
        csvwriter.writerows(rows)


def read_rows(filename):
    rows = []
    with open(filename, 'r', newline='') as csvfile:
        csvreader = csv.reader(csvfile)
        header = next(csvreader)
        if header[0] == 't_start' and header[1] == 'a_min' and header[2] == 'a_max' and header[3] == 'b':
            for row in csvreader:
                rows.append([int(row[0]), float(row[1]), float(row[2]), float(row[3])])
        elif header[0] == 'last_idx':
            rows = int(next(csvreader)[0])
        else:
            for row in csvreader:
                rows.append(row)

    return header, rows


def main(filenames, e_vals, online=False):
    results = []

    print('Merge online: ', online)
    print()

    for f in filenames:
        print('Time series:', f)
        ts = parse_file(f)
        ts_size = calculate_ts_size(len(ts))
        print('Size:', ts_size, 'bytes')

        for e in e_vals:
            print('e:', e)
            if not online:
                if exists(f[:-4] + '_Segments_' + str(e) + 'e.csv') and exists(
                        f[:-4] + '_SegmentsMetadata_' + str(e) + 'e.csv'):
                    _, segments = read_rows(f[:-4] + '_Segments_' + str(e) + 'e.csv')
                    _, last_idx = read_rows(f[:-4] + '_SegmentsMetadata_' + str(e) + 'e.csv')
                else:
                    segments, last_idx = swing_filter.apply(ts, e, init_structure_offline, insert_offline, quantization)
                    write_rows(f[:-4] + '_Segments_' + str(e) + 'e.csv', ['t_start', 'a_min', 'a_max', 'b'], segments)
                    write_rows(f[:-4] + '_SegmentsMetadata_' + str(e) + 'e.csv', ['last_idx'], [[last_idx]])
            else:
                tree_per_b, last_idx = swing_filter.apply(ts, e, init_structure_online, merge_online, quantization)
                segments = tree_per_b_to_array_of_segments(tree_per_b)
            swing_filter.validate(ts, swing_filter.reconstruct(segments, last_idx), e)
            wedges = len(segments)
            swing_size = calculate_swing_size(len(segments))
            print('Wedges:', wedges)
            print('Swing size:', swing_size, 'bytes')
            print('Compressio ratio:', round(ts_size / swing_size, 2))

            if not online:
                segments_merged, num_of_b = merge_offline(segments)
                swing_filter.validate(ts, swing_filter.reconstruct(unpack_merged(segments_merged), last_idx), e)
                wedges_merged = len(segments_merged)
                swing_size_merged = calculate_merged_swing_size(segments_merged)
                print('After merge')
                print('Wedges:', wedges_merged)
                print('Swing size:', swing_size_merged, 'bytes')
                print('Compressio ratio:', round(ts_size / swing_size_merged, 2))
                results.append(
                    [f, len(ts), e, num_of_b, wedges, swing_size, ts_size / swing_size, wedges_merged,
                     swing_size_merged, ts_size / swing_size_merged])
            else:
                results.append([f, len(ts), e, wedges, swing_size, ts_size / swing_size])

            a_diff_min = min(abs(segment[1] - segment[2]) for segment in segments)
            max_val = 0
            for segment in segments:
                if segment[2] > 0:
                    a_quantized = int(segment[2] / a_diff_min) * a_diff_min if a_diff_min else segment[2]
                    max_val = max(max_val, int(segment[2] / a_diff_min)) if a_diff_min else max(max_val, 0)
                else:
                    a_quantized = int(segment[1] / a_diff_min) * a_diff_min if a_diff_min else segment[1]
                    max_val = max(max_val, int(-segment[1] / a_diff_min)) if a_diff_min else max(max_val, 0)
                assert segment[1] <= a_quantized <= segment[2]
            assert max_val < 2**31 - 1

        print()

    if online:
        filename = 'results_online.csv'
        header = ['filename', 'length', 'e', 'wedges', 'size', 'cr']
    else:
        filename = 'results_offline.csv'
        header = ['filename', 'length', 'e', '#b', 'wedges', 'size', 'cr', 'wedges_merged', 'size_merged', 'cr_merged']

    write_rows(filename, header, results)


if __name__ == "__main__":
    files = [
        # 'dataset/AgiaParaskeviCleanTemp_Scaled.csv',
        # 'dataset/AristotelousCleanTemp_Scaled.csv',
        # 'dataset/AthensCleanTemp_Scaled.csv',
        # 'dataset/ElefsinaCleanTemp_Scaled.csv',
        # 'dataset/GeoponikiCleanTemp_Scaled.csv',
        # 'dataset/KoropiCleanTemp_Scaled.csv',
        # 'dataset/LiosiaCleanTemp_Scaled.csv',
        # 'dataset/LykovrisiCleanTemp_Scaled.csv',
        # 'dataset/MarousiCleanTemp_Scaled.csv',
        # 'dataset/NeaSmirniCleanTemp_Scaled.csv',
        # 'dataset/PatisionCleanTemp_Scaled.csv',
        # 'dataset/PeristeriCleanTemp_Scaled.csv',
        # 'dataset/PireusCleanTemp_Scaled.csv',
        # 'dataset/ThrakomakedonesCleanTemp_Scaled.csv'
        'dataset/Stocks-Germany_Scaled.csv',
        'dataset/Stocks-UK_Scaled.csv',
        'dataset/Stocks-USA_Scaled.csv'
    ]

    es = [
        1,
        .5,
        .4,
        .3,
        .2,
        .1,
        .05,
        .04,
        .03,
        .02,
        .01,
        .005
    ]

    main(files, es, online=False)
    # main(files, es, online=True)
