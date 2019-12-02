# -*- coding: utf-8 -*-
"""
Created on Thu Aug 22 21:30:29 2019

@author: tizan
"""

import pyrebase
import os
import random
import time
#import firebase_admin
#from firebase_admin import credentials

#funtion大駝峰，變數小駝峰

def UploadImageToFirebase(url,detectImageName,result):
   
   config = {
     "apiKey": "apiKey",
     "authDomain": "civic-kayak-240607.firebaseapp.com",
     "databaseURL": "https://civic-kayak-240607.firebaseio.com/",
     "storageBucket": "civic-kayak-240607.appspot.com",
     "serviceAccount": "civic-kayak-240607-firebase-adminsdk-7hd2n-e56339064e.json"
   }
   
   firebase = pyrebase.initialize_app(config)
   
   print("\n================Upload=================\n")
   
   #二進制轉字串
   url = str(url)

   print('url=',url)
   print('detectImageName=',detectImageName)
   print('result=',result)
   
   #decode url , get data path and storage path
   uid , house , date , _ , rid = url.split('/')[7].split('%2F',5)
   rid = rid.split('.')[0]
   recordDataPath = os.path.join('Record',uid,house,date,rid).replace('\\','/')
   #metaDataPath = os.path.join('MataData',uid,house)
   print('[Record Path] ',recordDataPath)
   storagePath = os.path.join(uid,house,date,'detectImage',detectImageName).replace('\\','/')
   print('[Storage Path] ',storagePath)
   
   #將結果圖存到storage
   storage = firebase.storage()
   storage.child(storagePath).put('detectImage\\'+detectImageName)
   print('上傳成功')
   detectImagePath ="https://firebasestorage.googleapis.com/v0/b/civic-kayak-240607.appspot.com/o/{0}%2F{1}%2F{2}%2FdetectImage%2F{3}?alt=media" \
   .format(uid,house,date,detectImageName)
   print('[Result Image Path]',detectImagePath)
   
   #將資料存到database
   db = firebase.database()
   db.child(recordDataPath).child('detectURL').set(detectImagePath)
   db.child(recordDataPath).child('result').set(result)
   
   #[測試]印出database節點資料
   #print('[Node Data]',end="")
   #print(db.child(recordDataPath).get().val())


#if __name__ == '__main__':
#   url='https://firebasestorage.googleapis.com/v0/b/civic-kayak-240607.appspot.com/o/wYtwwXdgpGfT626QQdCu5xquYQ32%2FG2%2F20190829%2ForiginImage%2F-LnTBPtsJKv4rxwodkcL.jpg?alt=media&token=65d17077-d046-4401-ba5f-d6da24126eb0'
#   UploadImageToFirebase(url,'test.jpg','Unhealth')


#server確定後此處改為環境變量
#reference:https://firebase.google.com/docs/admin/setup#prerequisites
#cred = credentials.Certificate("civic-kayak-240607-firebase-adminsdk-7hd2n-e56339064e.json")
#firebase_admin.initialize_app(cred)

