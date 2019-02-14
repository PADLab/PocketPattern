import csv
import sys

def main(args)
	with open(args[1], 'rb') as csvfile:
    	rowReader = csv.reader(csvfile, delimiter=',', quotechar='|')
    	for row in rowReader:
			print ', '.join(row)

main(sys.argv)