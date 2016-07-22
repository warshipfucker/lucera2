�� sr =com.l2jserver.script.java.JavaScriptEngine$JavaCompiledScript        L _classBytest Ljava/util/Map;L 
_classPatht Ljava/lang/String;xpsr java.util.HashMap���`� F 
loadFactorI 	thresholdxp?@     w      t custom.core.Buffer$1ur [B���T�  xp  �����   2 �  custom/core/Buffer$1  java/lang/Object  java/lang/Runnable this$0 Lcustom/core/Buffer; <init> (Lcustom/core/Buffer;)V Code	    
   	  ()V run
    !ru/catssoftware/L2DatabaseFactory   getInstance %()Lru/catssoftware/L2DatabaseFactory;
     getConnection ()Ljava/sql/Connection;
    custom/core/Buffer   ! access$0 %(Lcustom/core/Buffer;)Ljava/util/Map; # % $ java/util/Map & ' keySet ()Ljava/util/Set; ) + * java/util/Set , - iterator ()Ljava/util/Iterator; / 1 0 java/util/Iterator 2 3 next ()Ljava/lang/Object; 5 java/lang/Integer
 4 7 8 9 intValue ()I ; 2delete from character_buff_profiles where charId=? = ? > java/sql/Connection @ A prepareStatement 0(Ljava/lang/String;)Ljava/sql/PreparedStatement; C E D java/sql/PreparedStatement F G setInt (II)V C I J K execute ()Z C M N  close P 2insert into character_buff_profiles values (?,?,?)
 4 R S T valueOf (I)Ljava/lang/Integer; # V W X get &(Ljava/lang/Object;)Ljava/lang/Object; Z java/lang/String C \ ] ^ 	setString (ILjava/lang/String;)V ` custom/core/Buffer$BuffProfile
 _ b   c 2(Lcustom/core/Buffer$BuffProfile;)Ljava/util/List; e + f java/util/List / h i K hasNext = M	 l n m java/lang/System o p out Ljava/io/PrintStream; r java/lang/StringBuilder t Buffer: Can't save profiles 
 q v 	 w (Ljava/lang/String;)V
 q y z { append -(Ljava/lang/Object;)Ljava/lang/StringBuilder;
 q } ~  toString ()Ljava/lang/String;
 � � � java/io/PrintStream � w println � java/sql/SQLException StackMapTable EnclosingMethod InnerClasses BuffProfile 0             	 
          
*+� *� �           �  
  � � L*� � � " � ( N� �-� . � 4� 6=+:� < :� B � H W� L +O� < :� B *� � � Q� U � #:� " � ( :� ]� . � Y:� [ � U � _� a� d :	� $	� . � 4� 66� B � H W	� g ���� g ���� L -� g ��'+� j � L� k� qYs� u+� x� |� ��    � �  �   � �    =  /  � g   = / C #  /  � . 
  = / C # Y /  /   � 	   = / C #  /  �    =  /  �     �  �       �            _  � t custom.core.Buffer$BuffProfileuq ~   �����   2 !  custom/core/Buffer$BuffProfile  java/lang/Object _buffs Ljava/util/List; 	Signature %Ljava/util/List<Ljava/lang/Integer;>; this$0 Lcustom/core/Buffer; <init> (Lcustom/core/Buffer;)V Code	   	 

     ()V  javolution/util/FastList
  	     access$0 2(Lcustom/core/Buffer$BuffProfile;)Ljava/util/List; 7(Lcustom/core/Buffer;Lcustom/core/Buffer$BuffProfile;)V
     InnerClasses  custom/core/Buffer BuffProfile                 	 
           !     *+� *� *� Y� � �                *� �                 *+� �         
      t custom.core.Bufferuq ~   @�����   2�  custom/core/Buffer  ,ru/catssoftware/gameserver/model/quest/Quest _log Lorg/apache/log4j/Logger; qn Ljava/lang/String; htmlBase 	_lastPage Ljava/util/Map; 	Signature 6Ljava/util/Map<Ljava/lang/Integer;Ljava/lang/String;>; _isPetTarget 7Ljava/util/Map<Ljava/lang/Integer;Ljava/lang/Boolean;>; _err _buffprofiles gLjava/util/Map<Ljava/lang/Integer;Ljava/util/Map<Ljava/lang/String;Lcustom/core/Buffer$BuffProfile;>;>; _restoreDelays 4Ljava/util/Map<Ljava/lang/Integer;Ljava/lang/Long;>; saveProfiles Ljava/lang/Runnable; <clinit> ()V Code
    org/apache/log4j/Logger   	getLogger ,(Ljava/lang/Class;)Lorg/apache/log4j/Logger;	  !   # 50000_Buffer	  %   ' data/html/mods/buffer/Buffer	  ) 	  <init> , custom
  . * / ((ILjava/lang/String;Ljava/lang/String;)V 1 javolution/util/FastMap
 0 3 * 	  5   7  	  9  	  ;  	  =   ? custom/core/Buffer$1
 > A * B (Lcustom/core/Buffer;)V	  D  
 F H G 4ru/catssoftware/gameserver/datatables/NpcBufferTable I J getInstance 8()Lru/catssoftware/gameserver/datatables/NpcBufferTable;	  L 
 
 N P O #ru/catssoftware/gameserver/Shutdown I Q '()Lru/catssoftware/gameserver/Shutdown;
 N S T U registerShutdownHandler (Ljava/lang/Runnable;)V onFirstTalk �(Lru/catssoftware/gameserver/model/actor/instance/L2NpcInstance;Lru/catssoftware/gameserver/model/actor/instance/L2PcInstance;)Ljava/lang/String;
  Y Z W onTalk
 \ ^ ] <ru/catssoftware/gameserver/model/actor/instance/L2PcInstance _ ` getQuestState G(Ljava/lang/String;)Lru/catssoftware/gameserver/model/quest/QuestState;
  b c d newQuestState s(Lru/catssoftware/gameserver/model/actor/instance/L2PcInstance;)Lru/catssoftware/gameserver/model/quest/QuestState;
 \ f g h getObjectId ()Ljava/lang/Integer; j java/lang/StringBuilder
 l n m java/lang/String o p valueOf &(Ljava/lang/Object;)Ljava/lang/String;
 i r * s (Ljava/lang/String;)V u .htm
 i w x y append -(Ljava/lang/String;)Ljava/lang/StringBuilder;
 i { | } toString ()Ljava/lang/String;  � � java/util/Map � � put 8(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
 � � � java/lang/Boolean o � (Z)Ljava/lang/Boolean;
  � � � createBuffProfiles O(Lru/catssoftware/gameserver/model/actor/instance/L2PcInstance;)Ljava/util/Map;
 � � � )ru/catssoftware/gameserver/cache/HtmCache I � -()Lru/catssoftware/gameserver/cache/HtmCache;
 � � � � getHtm d(Ljava/lang/String;Lru/catssoftware/gameserver/model/actor/instance/L2PcInstance;)Ljava/lang/String;
  � � � fillHtml d(Lru/catssoftware/gameserver/model/actor/instance/L2PcInstance;Ljava/lang/String;)Ljava/lang/String; StackMapTable ShowLastPage (I)Ljava/lang/String;
 � � � java/lang/Integer o � (I)Ljava/lang/Integer;  � � � containsKey (Ljava/lang/Object;)Z  � � � get &(Ljava/lang/Object;)Ljava/lang/Object;
 � � � (ru/catssoftware/gameserver/model/L2World I � ,()Lru/catssoftware/gameserver/model/L2World;
 � � � � 	getPlayer A(I)Lru/catssoftware/gameserver/model/actor/instance/L2PcInstance; isValidTalker B(Lru/catssoftware/gameserver/model/actor/instance/L2PcInstance;Z)Z
 � � � 2ru/catssoftware/gameserver/model/olympiad/Olympiad I � 6()Lru/catssoftware/gameserver/model/olympiad/Olympiad;
 � � � � isRegistered A(Lru/catssoftware/gameserver/model/actor/instance/L2PcInstance;)Z
 \ � � � 	getTarget -()Lru/catssoftware/gameserver/model/L2Object; � =ru/catssoftware/gameserver/model/actor/instance/L2NpcInstance
 � � � 'ru/catssoftware/gameserver/util/L2Utils � � checkMagicCondition onEvent Y(Ljava/lang/String;Lru/catssoftware/gameserver/model/quest/QuestState;)Ljava/lang/String;
  � � � Z(Ljava/lang/String;Lru/catssoftware/gameserver/model/quest/QuestState;Z)Ljava/lang/String;	 \ � � � _event :Lru/catssoftware/gameserver/model/entity/events/GameEvent;
 � � � 8ru/catssoftware/gameserver/model/entity/events/GameEvent � � 	isRunning ()Z	 � � � ru/catssoftware/Config �  BUFFER_RESTRICTION � EVENT
 l � � � contains (Ljava/lang/CharSequence;)Z
 \ � � � isInJail � JAIL
 \ � � � getOlympiadGameId ()I � OLY
 \ � � � 
isInCombat � COMBAT
 \ � � � getKarma � KARMA
 \ � � � 
getPvpFlag ()B � PVP
 \ �  isInsideZone (B)Z SIEGE RB ARENA restoreCheck
 java/lang/Long	 � BUFFER_RESTORE_DELAY I
 java/lang/System currentTimeMillis ()J
	 o (J)Ljava/lang/Long;
	 	longValue
 1ru/catssoftware/gameserver/model/quest/QuestState � @()Lru/catssoftware/gameserver/model/actor/instance/L2PcInstance;
 \!" � isAlikeDead
 \$% � isAfraid
 \'( � isImmobilized* Chat
 l,-. 
startsWith (Ljava/lang/String;)Z
  �1 -not.htm
 �34 � booleanValue
 \678 getPet -()Lru/catssoftware/gameserver/model/L2Summon;:  
 l<=> indexOf (Ljava/lang/String;)I
 l@A � 	substringC 0
 lEF � equalsH -J -pet
 �LM. 
pathExistsO SelectProfile
 lQRS split '(Ljava/lang/String;)[Ljava/lang/String;
 \UVW getCharacterData !()Lru/catssoftware/util/StatsSet;Y BuffProfile
[]\ ru/catssoftware/util/StatsSet^_ set '(Ljava/lang/String;Ljava/lang/String;)Va -p2.htmc Profilee -p1.htmg ClearProfile
 ijk getActiveProfile `(Lru/catssoftware/gameserver/model/actor/instance/L2PcInstance;)Lcustom/core/Buffer$BuffProfile;
mon custom/core/Buffer$BuffProfilepq access$0 2(Lcustom/core/Buffer$BuffProfile;)Ljava/util/List;sut java/util/Listv  clearx DeleteProfile
 z{| getActiveProfileName R(Lru/catssoftware/gameserver/model/actor/instance/L2PcInstance;)Ljava/lang/String; ~ � remove� CreateProfile� IИмя профиля не должно содержать пробелы
m� *� 7(Lcustom/core/Buffer;Lcustom/core/Buffer$BuffProfile;)V� 
UseProfile
��� ,ru/catssoftware/gameserver/model/L2Character�  stopAllEffectss��� iterator ()Ljava/util/Iterator;��� java/util/Iterator�� next ()Ljava/lang/Object;
 ��� � intValue  �P
 ��� � getNpcId
 F��� getSkillInfo (II)[I
 ��� useBuff �(Lru/catssoftware/gameserver/model/actor/instance/L2NpcInstance;I[ILru/catssoftware/gameserver/model/actor/instance/L2PcInstance;Lru/catssoftware/gameserver/model/L2Character;)Z��� � hasNext� RemBuff
 � � �� 	GM Buffer	 ��� GMSHOP_BUFF_ITEM	 ��� GMSHOP_BUFF_REMOVE
 \��� destroyItemByItemId C(Ljava/lang/String;IILru/catssoftware/gameserver/model/L2Object;Z)Z� recHp
 � �	 ��� GMSHOP_BUFF_CP
���� 	getStatus <()Lru/catssoftware/gameserver/model/actor/status/CharStatus;
��� � getMaxHp
��� 8ru/catssoftware/gameserver/model/actor/status/CharStatus�� setCurrentHp (D)V� recCp
��� � getMaxCp
���� setCurrentCp� recMp	 ��� GMSHOP_BUFF_MP
��� � getMaxMp
���� setCurrentMp� Target� Buff
 � � �� BuffPet� TNPC Buffer Warning: buffer has no buffGroup set in the bypass for the buff selected.
 ��� warn (Ljava/lang/Object;)V
 ���> parseInt� NPC Buffer Warning: Player: 
 \�� } getName�  has tried to use skill group (
 i� x� (I)Ljava/lang/StringBuilder;� !) not assigned to the NPC Buffer!� java/lang/Exception [Ljava/lang/String; [I
[ 	getString &(Ljava/lang/String;)Ljava/lang/String;	 Нет "java/lang/IllegalArgumentException %target% 
Слуга Персонаж
 l replace D(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String; 	%profile% %err%  keySet ()Ljava/util/Set;�  java/util/Set" F<tr><td><center><a action="bypass -h Quest 50000_Buffer SelectProfile $ ">& </a></center></td></tr>
 l() � length+ 9<tr><td><center>Отсутствуют</center></td></tr>- %profilelist%s/0 � size2 %useprofile%4 �<button action="bypass -h Quest 50000_Buffer UseProfile" value="Исп. профиль" width=100 height=21 back="sek.cbui94" fore="sek.cbui92">6 Aru/catssoftware/gameserver/model/actor/instance/L2MonsterInstance8 <tr><td><center>
:<; 0ru/catssoftware/gameserver/datatables/SkillTable I= 4()Lru/catssoftware/gameserver/datatables/SkillTable;
:?@ � getSkillNameB </center></td></tr>D 	%buflist%F Error getting profiles for H 
, cleaning
 JK� errorM �<html><body><br><center>Произошла ошибка при обрашении к баферу, попробуйте позже</center></body></html>
 \OPQ getInventory >()Lru/catssoftware/gameserver/model/itemcontainer/PcInventory;
SUT :ru/catssoftware/gameserver/model/itemcontainer/PcInventoryVW getItemByItemId 4(I)Lru/catssoftware/gameserver/model/L2ItemInstance;
Y[Z /ru/catssoftware/gameserver/model/L2ItemInstance\ � isStackable
S^_` getInventoryItemCount (II)Ib >ru/catssoftware/gameserver/network/serverpackets/SystemMessage	dfe 2ru/catssoftware/gameserver/network/SystemMessageIdgh NOT_ENOUGH_ITEMS 4Lru/catssoftware/gameserver/network/SystemMessageId;
aj *k 7(Lru/catssoftware/gameserver/network/SystemMessageId;)V
 \mno 
sendPacket H(Lru/catssoftware/gameserver/network/serverpackets/L2GameServerPacket;)Vq 
Npc Buffer
:stu getInfo .(II)Lru/catssoftware/gameserver/model/L2Skill;	 �wxy BUFFER_ANIMATION Z{ Cru/catssoftware/gameserver/network/serverpackets/MagicSkillLaunched
}~ (ru/catssoftware/gameserver/model/L2Skill� h getId
}�� � getLevel� )ru/catssoftware/gameserver/model/L2Object
z� *� `(Lru/catssoftware/gameserver/model/L2Character;IIZ[Lru/catssoftware/gameserver/model/L2Object;)V� >ru/catssoftware/gameserver/network/serverpackets/MagicSkillUse
�� *� d(Lru/catssoftware/gameserver/model/L2Character;Lru/catssoftware/gameserver/model/L2Character;IIIIZ)V      �
��� java/lang/Thread�� sleep (J)V
}��� 
getEffects �(Lru/catssoftware/gameserver/model/L2Character;Lru/catssoftware/gameserver/model/L2Character;)[Lru/catssoftware/gameserver/model/L2Effect;s� � �s�� � add� java/lang/InterruptedException �(Lru/catssoftware/gameserver/model/actor/instance/L2PcInstance;)Ljava/util/Map<Ljava/lang/String;Lcustom/core/Buffer$BuffProfile;>;
��� !ru/catssoftware/L2DatabaseFactory I� %()Lru/catssoftware/L2DatabaseFactory;
���� getConnection ()Ljava/sql/Connection;� Iselect * from character_buff_profiles where charId=? order by profileName��� java/sql/Connection�� prepareStatement 0(Ljava/lang/String;)Ljava/sql/PreparedStatement;��� java/sql/PreparedStatement�� setInt (II)V���� executeQuery ()Ljava/sql/ResultSet;� profileName�� java/sql/ResultSet� 	buffGroup���> getInt��� ����  close����� Buffer: Can't load buf profiles
 �K� *(Ljava/lang/Object;Ljava/lang/Throwable;)V� java/sql/SQLException main ([Ljava/lang/String;)V	 ���y BUFFER_ENABLED
  3 %(Lcustom/core/Buffer;)Ljava/util/Map; InnerClasses !     	 
     	     
 	     
                                                           � �  "� $&� (�      *      c     W*� $+� -*� 0Y� 2� 4*6� 8*� 0Y� 2� :*� 0Y� 2� <*� >Y*� @� C� EW*� 0Y� 2� K� M*� C� R�      V W          *+,� X�      Z W     �     o,� $� [� 	*,� aW*� K,� e� iY� (� k� qt� v� z� ~ W*� 4,� e� �� ~ W*,� �W� �� iY� (� k� qt� v� z,� �N*,-� ��    �      � �     R     =*� K� �� � � .� �*� K� �� � � l� �� �� �M*� �� �,� ���    �    ;  � �     D     ,� �+� �� �+� �� +� ��  � +� ę ��    �    
  � �          *+,� ˰      � �     �     �=+� �� +� ζ ҙ � �ݶ ߙ =+� � � �� ߙ =+� � � �� ߙ =+� � � �� ߙ =+� � � ��� ߙ =+� �� � ��� ߙ =+� �� � �� ߙ =+� �� � �� ߙ =+� �� � �� ߙ =�    �    	�    �     {     _*� <+� e� � �	M�� �,� *� <+� e��� ~ W����h�a,��� *� <+� e��� ~ W��    �   
 � 	)  � �    	     e,�:� �� � �#� �&� +)�+� �*�/� "� �� iY� (� k� q0� v� z� ��:�  � �� � ��  � �� �::*� 4� e� � � ��2� �5� 
�5:+)�+�6:� iY� (� k� qt� v� z:+9�;� ++9�;`�?:B�D� �� iYG� q� v� z:*� 4� e� � � ��2� p� �� iY� (� k� qI� v� vt� v� z�K� (� iY� (� k� qI� v� vt� v� z:� >� iY� (� k� q� vt� v� z:� � iY� (� k� q� vt� v� z:*� K� e� ~ W� �� �:	*	� ��+N�+� m+9�P:�TX2�Z� �� iY� (� k� q`� v� z� �:*� K� e� iY� (� k� q`� v� z� ~ W*� �:�+b�+� Q� �� iY� (� k� qd� v� z� �:*� K� e� iY� (� k� qd� v� z� ~ W*� ��+f�+� *�h:�l�r *b,� ˰+w�+� C*� :� e� � � :*�h� *�y�} W�TX�Z*b,� ˰+��+� �*� :� e� � � :� ,*� :� e� 0Y� 2� ~ W*� :� e� � � :+9�P:�� *�� 8*b,� ˰2� � � 2�mY*��� ~ W�TX2�Z*b,� ˰+��+� i*�h:�~���l�� :	� ;	�� � ���6� E� 	�� ����:
*
��W	�� ����%+��+� )*����������������+��+� ;*����*����������������Ň�ȧ�+ζ+� ;*����*����������������Ї�ӧq+ֶ+� ;*���^*���T��������@���ۇ�ާ/+�+� 4*� 4� e*� 4� e� � � ��2� � � �� ~ W� �+�+� �*��� *� e����+�+� +�?9�P:� +�?9�P:Y:�6
6	� �	2:� �  ��*� e������6� E� 	�� ����:� <�  � iY� q��� v�� v���� v� z��*� e����*��W�		
��i*� e����W� iY� (� k� qt� v� z�    N�  /N� 1 XN� Y�N��>N�?�N���N��N�rN�s�N��pN�q�N��'N�(MN�  �  9 2�  \'� # �� )�� > l l� �� %� s� W%� 3 � � J �   "� � * 
  l \ ��m �  �  
  l \ ��m�  F�  
  l \ ��m�  F�  
  l \ ��m �  � 	   l \ ��m  � ,>>>� -   l \ ��   ��     l \ ��   � �  �    l \ ��     � !   l \ ��  l   �    l \ ��  l   F�    l \ ��  l   F� D�    l \ ��     �    l \ ��  �    l � {|     0     +�TX��W�    
 
  �    K
 jk     o     9M+�TX�M� W�*� :+� e� � � N,� -� -,� � �m��    
  �    �    \ l 
� #  " � �        �,*� 4+� e� � � ��2� 	� �M,*+�y�M,*� 8�M*6� 86N*� :+� e� � � :� U� � :� :�� � l:� iY-� k� q!� v� v#� v� v%� v� zN�� ���-�'� *N,,-�M*+�h:� ��l� ��l�. � �,13�M6:�6�l�� :	� o	�� � �:+� �� #+� ��  +� ��5� +� �� ¶�6� E����:
� iY� k� q7� v�9
.�>� vA� v� z:	�� ���,C�M� I,16�M� <W,16�M*� :+� e�} W�  � iYE� q+��� vG� v� z�I,�WL�  I���  ���  �   �     \ l  l l�    \ l  l l l� M   \ l l  �  6� 	   \ l l   
� L 
  \ l l m l �  � 2 
  \ l l m l ��  � 8 
  \ l l m l �  �    \ l l m  	�    \ l l �8�    \ l � ��    �    h6�\-.6-`.6-`.6	-`.6
	� ��N	�R:� �X� (�N	�]
� �aY�c�i:�l��X� -p	
� ���� :�aY�c�i:�l�6� p	� ���W�
���9�r:� �+� X�v� R�zY+�|������YS���l��Y+�|��������l���� W��W*�h:� #�l� ��� � �l� ��� W�-����� !$�  �   k � � K   � \�Y  1� � � h   � \�} � � 6   � \�    � �     �   +     �*� :+� e� � � M,� ƻ 0Y� 2M*� :+� e,� ~ W����N-��� :+� e���� �� :6::� O��� �D� %��� :�mY*��:,� ~ W�l¹� � ��� W�� ����� �� -�� � N�  �-��,�  , � ��  �   7 � `   \ ��� lm  3�     \  �
 	��     "     �י 	� �ڱ    �    p�          *� :�     �     >      m Y xt CG:\c6\datapack\data\scripts;G:\c6\gameserver\build\.\gameserver.jar