import xml.etree.ElementTree as ET
import csv
import sys

def extract_data(xml_file, csv_file):
    # Parse the XML file
    tree = ET.parse(xml_file)
    root = tree.getroot()

    # Open CSV file for writing
    with open(csv_file, 'w', newline='') as csvfile:
        writer = csv.writer(csvfile)

        # Write header
        writer.writerow(['Package Name', 'Class Name', 'Covered Lines'])

        # Iterate over packages
        for package in root.findall('.//package'):
            package_name = package.get('name')

            # Iterate over source files
            for sourcefile in package.findall('sourcefile'):
                class_name = sourcefile.get('name')
                covered_lines = []

                # Iterate over lines with non-zero coverage
                for line in sourcefile.findall('line[@ci!="0"]'):
                    covered_lines.append(line.get('nr'))

                # Write row to CSV if there are covered lines
                if covered_lines:
                    writer.writerow([package_name, class_name, ';'.join(covered_lines)])

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python script.py <input_jacoco_xml_report> <output_extracted_data.csv>")
        sys.exit(1)

    input_xml = sys.argv[1]
    output_csv = sys.argv[2]

    extract_data(input_xml, output_csv)