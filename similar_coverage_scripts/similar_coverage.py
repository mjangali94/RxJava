import csv
import os
import io
import pathlib
import sys
import shutil

ju2jmh_threshold = 20  # 10%

class CoverageData(dict):
    pass

def main():
    folder_path = "individual_coverage_reports"

    benchmark_folders = [f for f in os.listdir(folder_path) if os.path.isdir(os.path.join(folder_path, f))]

    total_ju2jmh = sum("_Benchmark.benchmark_" in folder for folder in benchmark_folders)
    total_jmh = len(benchmark_folders) - total_ju2jmh
    total_comparisons = total_ju2jmh * total_jmh

    completed_comparisons = 0
    percent_complete = 0

    for benchmark_folder in benchmark_folders:
        if not os.path.isdir(os.path.join(folder_path, benchmark_folder)):
            continue

        is_ju2jmh = "_Benchmark.benchmark_" in benchmark_folder

        if not is_ju2jmh:
            continue

        coverage_data = get_coverage_data(os.path.join(folder_path, benchmark_folder))

        for other_benchmark_folder in benchmark_folders:
            if not os.path.isdir(os.path.join(folder_path, other_benchmark_folder)) or other_benchmark_folder == benchmark_folder:
                continue

            is_jmh = "_Benchmark.benchmark_" not in other_benchmark_folder

            if not is_jmh:
                continue

            other_coverage_data = get_coverage_data(os.path.join(folder_path, other_benchmark_folder))

            intersection_coverage = calculate_intersection_coverage(coverage_data, other_coverage_data)

            total_lines_ju2jmh = sum(len(lines) for classes in coverage_data.values() for lines in classes.values())

            total_common_lines = sum(len(lines) for classes in intersection_coverage.values() for lines in classes.values())

            percentage = total_common_lines / total_lines_ju2jmh * 100

            if percentage > ju2jmh_threshold:
                print(f"For jmh benchmark {other_benchmark_folder}, ju2jmh benchmark {benchmark_folder} has an overlapping coverage of {percentage}.")
                print_similarities(intersection_coverage, coverage_data, other_coverage_data)

            # completed_comparisons += 1
            # new_percent_complete = completed_comparisons * 100 / total_comparisons
            # if new_percent_complete > percent_complete:
            #     percent_complete = new_percent_complete
            #     print(f"Progress: {percent_complete}%")

def get_coverage_data(directory):
    coverage_data = CoverageData()

    csv_file_path = os.path.join(directory, "report.csv")
    with open(csv_file_path, newline='') as csv_file:
        csv_reader = csv.reader(csv_file)
        for row in csv_reader:
            package_name = row[0]
            class_name = row[1]
            lines_covered = [int(line) for line in row[2].split(";") if line]
            coverage_data.setdefault(package_name, {}).setdefault(class_name, []).extend(lines_covered)

    return coverage_data

def calculate_intersection_coverage(data1, data2):
    intersection = CoverageData()

    for package_name, classes1 in data1.items():
        if package_name in data2:
            intersection.setdefault(package_name, {})
            for class_name, lines1 in classes1.items():
                if class_name in data2[package_name]:
                    intersection[package_name].setdefault(class_name, [])
                    lines2 = data2[package_name][class_name]
                    intersection[package_name][class_name] = [line for line in lines1 if line in lines2]

    return intersection

def print_similarities(intersection, data1, data2):
    print("Similarities:")
    for package_name, classes in intersection.items():
        for class_name, lines in classes.items():
            if lines:
                print(f"Package: {package_name}, Class: {class_name}, Lines: {lines}")

if __name__ == "__main__":
    main()
