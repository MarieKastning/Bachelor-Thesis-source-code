#!/usr/bin/env python

from __future__ import print_function
from subprocess import call

import sys
import os
import argparse
import glob
import random
import subprocess as sp
import multiprocessing as mp


def work(in_file):
    """Defines the work unit on an input file"""
    # each line in the file contains a graph-file
    print(in_file)
    split_line = in_file.split("/")
    split_line = split_line[-1]

    for i in range(1, 16):
        sp.call(["bash", "test-instance.txt", in_file, str(i), split_line + ".time"])
    return 0

if __name__ == '__main__':
    files = []
    # list of files
    for line in open("graph_files.txt"):
        if not line.startswith("#"):
            files += [line.strip()]

    # Set up the parallel task pool to use all available processors
    count = 24
    random.shuffle(files)
    pool = mp.Pool(processes=count)

    # Run the jobs parallel

    pool.map(work, files)
