import json
import os
import shutil
from datetime import datetime
import lottie

import sys

sys.path.insert(0, os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "lib"
))

from lottie.utils import script
from math import ceil

folder_name = "<Main folder with json export>"
extra_imports_folders = ['<Extra folders with json export']
user_id_to_name_dict = {'user274428442': 'Juan'}
dir_principal = os.path.abspath(folder_name)

output_counter = 0
root_output = os.path.join(dir_principal, 'output')

if os.path.exists(root_output) and os.path.isdir(root_output):
    shutil.rmtree(root_output)
os.mkdir(root_output)

output = os.path.join(root_output, 'pack' + str(output_counter))
os.mkdir(output)
out_messages = open(os.path.join(output, 'Chat de WhatsApp con.txt'), 'w', encoding="utf8")

extra_imports_folders.insert(0, folder_name)

num_lines = 0
counter = 0
for current_folder_name in extra_imports_folders:
    actual_dir = os.path.abspath(current_folder_name)
    file = open(actual_dir + "/result.json", 'r', encoding="utf8")
    file_json = json.load(file)
    num_lines += len(file_json['messages'])

file_counter = 1
for current_folder_name in extra_imports_folders:
    actual_dir = os.path.abspath(current_folder_name)
    dir_name = os.path.basename(os.path.normpath(actual_dir))

    file = open(actual_dir + "/result.json", 'r', encoding="utf8")

    file_json = json.load(file)

    for message in file_json['messages']:
        try:
            if file_counter > 1000:
                file_counter = 1
                output_counter += 1
                output = os.path.join(root_output, 'pack' + str(output_counter))
                os.mkdir(output)
                out_messages.close()
                out_messages = open(os.path.join(output, 'Chat de WhatsApp con.txt'), 'w', encoding="utf8")

            #print(message)
            counter += 1
            if message['type'] == 'message':
                if counter % (ceil(num_lines / 300)) == 0:
                    print("Progress reading messages (Pack " + str(output_counter) + " with " + str(
                        file_counter) + " files): " + str(counter) + "/" + str(num_lines) + " (" + str(
                        round((counter / num_lines) * 100)) + "%).")
                out_line = ''
                date = datetime.strptime(message['date'], '%Y-%m-%dT%H:%M:%S')

                out_line += date.strftime('%d/%m/%y, %H:%M')
                out_line += " - "

                if 'from' in message and message['from'] is not None:
                    out_line += message['from']
                else:
                    out_line += user_id_to_name_dict[message['from_id']]

                out_line += ": "

                if 'location_information' in message:
                    location = message['location_information']
                    out_line += 'https://maps.google.com/?q=' + str(location['latitude']) + ',' + str(
                        location['longitude']) + '\n'
                elif 'file' in message and message[
                    'file'] != '(File exceeds maximum size. Change data exporting settings to download.)':

                    file = os.path.join(actual_dir, message['file'])
                    head, filename = os.path.split(file)
                    name, extention = os.path.splitext(filename)

                    name = dir_name + "_" + name
                    filename = dir_name + "_" + filename

                    if extention == '.tgs':
                        if not (os.path.exists(os.path.join(output, name + ".webp")) and os.path.isfile(
                                os.path.join(output, name + ".webp"))):
                            file_counter += 1

                            animation = lottie.parsers.tgs.parse_tgs(file)
                            animation.in_point = 30
                            animation.out_point = 30
                            script.script_main(animation=animation, basename=name, path=output, formats=['webp'],
                                               verbosity=0)

                        out_line += name + ".webp"
                    else:
                        if not (os.path.exists(os.path.join(output, filename)) and os.path.isfile(
                                os.path.join(output, filename))):
                            file_counter += 1

                            shutil.copy2(file, output + "/" + filename)
                        out_line += filename
                    out_line += " (archivo adjunto)\n"

                elif 'photo' in message:

                    file = os.path.join(actual_dir, message['photo'])
                    head, filename = os.path.split(file)

                    filename = dir_name + "_" + filename

                    if not (os.path.exists(os.path.join(output, filename)) and os.path.isfile(
                            os.path.join(output, filename))):
                        file_counter += 1

                        shutil.copy2(file, output + "/" + filename)
                    out_line += filename
                    out_line += " (archivo adjunto)\n"

                if 'text' in message and message['text'] != '':
                    text = message['text']
                    if type(text) == str:
                        out_line += text
                    else:
                        for t in text:
                            if type(t) == str:
                                out_line += t
                            else:
                                out_line += t['text']
                    out_line += '\n'
                out_messages.write(out_line)
        except Exception as e:
            print(e)
            print(message)
            exit(1)

out_messages.close()

print("Zipping...")
shutil.make_archive(os.path.join(dir_principal, 'output'), 'zip', root_output)
