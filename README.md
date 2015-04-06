# smartcard-truecrypt
Using TrueCrypt with a JavaCard

Hello all,
in this project I want to further develop my application from my Bachelorthesis.

The app stores the user container passwords on a SmartCard and automatically loads the passwords from it to the choosen Containers. The user just remember a four digit PIN to access the SmartCard. 
Additionally I've implented an password-share methode. The user is able to share passwords with previous stored PublicKeys (RSA 2048 Bit). The private key ist generated at initialsition of the card and dont leave this.

At the moment it only runs on Windows, because of some console commands to control TrueCrypt. I try to add the issues, so that they are easier to find. ;-)

My personal target is, to advance to an easy to use full-encryption. 

Everbody is welcome to review my Code and to add improvements. ;-)

Now some data of used Hardware:
  SmartCard: JCOP 2.4.1 R3A (J3A)
  Link: http://www.nxp.com/documents/line_card/939775017001_v9_HR.pdf
  
  
