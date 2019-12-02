import os
import requests
import OD
import upload
import random

os.makedirs('./TestSet/',exist_ok=True)
IMG_DIR="./TestSet/"
IMG_TYPE=".jpg"

url='https://firebasestorage.googleapis.com/v0/b/civic-kayak-240607.appspot.com/o/286G-000-1D1.jpg?alt=media&token=aa329a64-782e-4c3f-9041-3475061c64d1'
def downloadIMG(url ,DIR , FILENAME , TYPE):
    r=requests.get(url)
    with open(DIR + FILENAME + TYPE,'wb') as f:
        f.write(r.content)
def disease_detector(url):

    IMG_FILENAME = str(random.uniform(0,1))
    downloadIMG(url , IMG_DIR, IMG_FILENAME ,IMG_TYPE)
    ######### Uid
    
    #########
    cmd = "python OD.py splash --weight=last --image=\""+IMG_DIR + IMG_FILENAME + IMG_TYPE +"\""
    os.system(cmd)
    #*#
    result = OD.getResult()
    
    print('url=',url)
    print('DetectedIMG=',IMG_FILENAME+'.jpg')
    print('result=',result)
    
    upload.UploadImageToFirebase(url,IMG_FILENAME+'.jpg',result)
       

#disease_detector(url)
