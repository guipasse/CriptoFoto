# CriptoFoto
Secure photo and gallery application

CriptoFoto is a mobile application for Android devices that is able to take a picture, 
store it and display it in a photo gallery safely.

In order to implement the security of these photographs, the application uses a key 
provided by the user to encrypt the photos before storing them in the filesystem.

Subsequently, the application is able to decipher these photos using the password 
of the user in order to display them in a gallery.

The key used to cipher in this symmetric encryption system is not stored on the 
mobile phone.

This application also implements the basic operations of this type of 
applications, such as removing pictures, displaying pictures in full screen ....

In addition, the application provides a record of intrusions that stores data from
each illegal access and makes a photograph of the attacker, so the user can consult 
this register and see who tried to illegally access their photographs.

The data and photographs log intrusions are also encrypted before being stored in the
file system, so in order to perform this task, the application uses two cryptographic 
systems, one asymmetrical and another symmetrical, which work together to ensure the 
security of these data and photographs.

Enjoy it!!

The author: Guillem Pascual Serra

For any comments or suggestions you can write me here... ktekesis@hotmail.com
