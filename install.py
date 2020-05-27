import argparse
import os

parser = argparse.ArgumentParser(description="Setup the database")
parser.add_argument("version", help="The version of the database we want to run",
                    choices=['1.0.0'])
parser.add_argument("-d", "--datasource", help="The datasource you wanna use", default=USERNAME)
parser.add_argument("-m", "--micrometer", help="the library for micrometer you wanna use", default=PASSWORD)
parser.add_argument("-u", "--username",
                    help="the main user for the service",
                    default=None)


def main():
    connection.cursor().execute('USE ObserveRTC')
    print(args.version)
    files = os.listdir(args.version)  # list of directory files
    files.sort()
    offset_active = args.offset is not None
    break_active = args.breakpoint is not None
    for filename in files:
        print("Open file", filename)
        if offset_active:
            if filename != args.offset:
                print("Skip this file, due to offset param")
                continue
            offset_active = False

        if break_active:
            if filename == args.breakpoint:
                print("Program reached breakpoint, we stop execution")
                break_active = False
                break

        with open("/".join([args.version, filename]), 'r') as file:
            sql_script = file.read()
            print("Content of ", filename, "has been read")
            with connection.cursor() as cursor:
                print("Executing scripts: ", sql_script)
                cursor.execute(sql_script)
    connection.commit()
    return 0


if __name__ == "__main__":
    main()
