# -*- coding:UTF-8 -*-
from socket import AF_INET, socket, SOCK_STREAM
from threading import Thread
import tkinter
import time
import random
from PIL import Image, ImageTk
import pdb


def receive():
    """Handles receiving of messages."""
    while True:
        try:
            mms =time.strftime('%Y-%m-%d %H:%M:%S', time.localtime()) + '\n'
            msg = client_socket.recv(BUFSIZ).decode("utf8")
            msglist.insert(tkinter.END, mms,'green')
            msglist.insert(tkinter.END, msg)
        except OSError:  # Possibly client has left the chat.
            break


def send():  # event is passed by binders.
    """Handles sending of messages."""
    msg = my_msg.get('0.0',tkinter.END)
    my_msg.delete('0.0',tkinter.END)  # Clears input field.
    client_socket.send(bytes(msg, "utf8"))

    if msg == "{quit}":
        client_socket.close()
        top.quit()


def on_closing():
    """This function is to be called when the window is closed."""
    my_msg.insert(tkinter.END,'{quit}')
    send()


def sendEvent(event):
    if event.keysym == 'Return':
        send()

def cancel():
    my_msg.delete('0.0', tkinter.END)



def red():
    msglist.config(fg="red")



def green():

       msglist.config(fg="green")
def blue():

       msglist.config(fg="blue")

def color1():
    names = ['red', 'green', 'blue', 'yellow', 'white', 'SlateGray', 'SpringGreen', 'LightSteelBlue', 'Cyan',
             'GhostWhite','BlueViolet']

    for i in names:
        msglist.config(fg=i)
        time.sleep(0.4)
        msglist.update()


def message():
    messagebox.showinfo("联系人搜索","这是一个假的功能！！")



def fontSize(ev=None):
    msglist.config(font='Helvetica -%d bold' % sizeScale.get())
    sizeLabel.config(text='size：%d'% sizeScale.get())




top = tkinter.Tk()
top.title("Chatter")
top.resizable(0,0)


#第一列
frmA1 = tkinter.Frame(width=180, height=30)
frmA2 = tkinter.Frame(width=180, height=300)
frmA3 = tkinter.Frame(width=180, height=140)
frmA4 = tkinter.Frame(width=180, height=30)

#第二列
messages_frame = tkinter.Frame(width=350, height=330)
send_frame = tkinter.Frame(width=350, height=140)
button_frame = tkinter.Frame(width=350, height=30)


my_msg = tkinter.Text(send_frame,font=("Times", "11", 'bold'),width=53, height=10,)  # For the messages to be sent.
my_msg.bind("<KeyPress-Return>", sendEvent)


msglist=tkinter.Text(messages_frame,font=("Times", "11", 'bold'),width=46, height=24,)
msglist.tag_config('green',foreground='green')


#txtText = tkinter.Text(frmC11, font=("Times", "11", 'bold'),width=24, height=18,  spacing2=5, bd=2, padx=5, pady=5,selectbackground='blue', state = tkinter.NORMAL)

scroLianxi = tkinter.Scrollbar(frmA2,width=22,cursor='pirate',troughcolor="blue")



listLianxi = tkinter.Listbox(frmA2, width=18,height=18,yscrollcommand = scroLianxi.set )
Linkman=['Amy','Tom','Petter','Selina','Ann','Bebby','Bob','dad','Hebe','Helen','Linda','Lucy','Zara','Zim',
         'Tom','Petter','Selina','Ann','Bebby','Bob','dad','Hebe','Helen','Danny','Dannel','Fani','Yuan','Zhi',
         'dad', 'Hebe', 'Helen', 'Danny', 'Dannel', 'Fani', 'Yuan', 'Zhi']

for line in Linkman:
        listLianxi.insert(tkinter.END, "  contacts   ------   " + str(line))
scroLianxi.config( command = listLianxi.yview )




send_button = tkinter.Button(button_frame,width =8, cursor ='heart', text="Send", command=send)
cancel_button = tkinter.Button(button_frame,width= 8,cursor='shuttle', text="cancel", command=cancel)


ConSerch=tkinter.Button(frmA1, text='Search contacts',
                  width = 9,height=1,
                  cursor='man',
                  command =message)

entrySerch=tkinter.Entry(frmA1,bd=3,width=14,show='*')

#Canvas
'''
imgCanvas=tkinter.Canvas(frmC2,bg='ivory')
image = Image.open('/Users/yiyuanfu/小黄人.jpg')
image = image.resize((180, 140))
im = ImageTk.PhotoImage(image)
#pdb.set_trace()
imgCanvas.create_image(80,80,image=im)
'''
###
'''
imgCanvas1=tkinter.Canvas(frmA3,bg='ivory')
image1 = Image.open('/Users/yiyuanfu/小黄人.jpg')
image1 = image1.resize((180, 140))
im1 = ImageTk.PhotoImage(image1)
#pdb.set_trace()
imgCanvas1.create_image(80,80,image=im1)

'''
#Radiobutton


#Menubutton
colorMenubt = tkinter.Menubutton (button_frame, text="color", relief=tkinter.RAISED )

#menu

colorMenubt.menu = tkinter.Menu(colorMenubt, tearoff=0)
colorMenubt["menu"] = colorMenubt.menu

colorMenubt.menu.add_checkbutton(label="red", command=red)
colorMenubt.menu.add_checkbutton(label="green", command=green)
colorMenubt.menu.add_checkbutton(label="blue", command=blue)
colorMenubt.menu.add_separator()  # 添加菜单分隔符
colorMenubt.menu.add_checkbutton(label="color1", command=color1)


#scale
sizeScale = tkinter.Scale(button_frame,length=80,width=18,from_=10, to=20,orient=tkinter.HORIZONTAL,command=fontSize,cursor='star',
                    showvalue=0,
                    sliderlength=20,
                    troughcolor='ivory')

sizeScale.set(11)

#11.Label控件
sizeLabel = tkinter.Label(button_frame,width=8,height=1,bd=1, relief=tkinter.RIDGE)


#窗口布局


frmA1.grid(row=0, column=0, padx=10, pady=3)
frmA2.grid(row=1, column=0, padx=10)
frmA3.grid(row=2, column=0, rowspan=1)
frmA4.grid(row=3, column=0, rowspan=1)

messages_frame.grid(row=0, column=1, columnspan=1, rowspan=2, padx=1, pady=3)
send_frame.grid(row=2, column=1, columnspan=1, padx=1, pady=3)
button_frame.grid(row=3, column=1, columnspan=1, padx=3)


#固定大小

frmA1.grid_propagate(0)
frmA2.grid_propagate(0)
frmA3.grid_propagate(0)
#frmA4.grid_propagate(0)

messages_frame.grid_propagate(0)
send_frame.grid_propagate(0)
button_frame.grid_propagate(0)

#控件布局

send_button.grid(row=0, column=0)

cancel_button.grid(row=0, column=1)

ConSerch.grid(row=0, column=1)
entrySerch.grid(row=0, column=0)


sizeLabel.grid(row=0, column=3)

msglist.grid()
my_msg.grid()



scroLianxi.grid(row=0, column=1, ipady=120)


listLianxi.grid(row=0, column=0)

#imgCanvas1.grid(row=0, column=0, sticky=tkinter.N)

colorMenubt.grid(row=0, column=5)




sizeScale.grid(row=0, column=4)


top.protocol("WM_DELETE_WINDOW", on_closing)


HOST = input('Enter host: ')
PORT = input('Enter port: ')
if not PORT:
    PORT = 899
else:
    PORT = int(PORT)

BUFSIZ = 1024
ADDR = (HOST, PORT)

client_socket = socket(AF_INET, SOCK_STREAM)
client_socket.connect(ADDR)

receive_thread = Thread(target=receive)
receive_thread.start()
tkinter.mainloop()  # Starts GUI execution.