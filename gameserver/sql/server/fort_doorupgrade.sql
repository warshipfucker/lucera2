DROP TABLE IF EXISTS `fort_doorupgrade`;
CREATE TABLE `fort_doorupgrade` (
  `doorId` int(11) NOT NULL DEFAULT '0',
  `fortId` int(11) NOT NULL,
  `hp` int(11) NOT NULL DEFAULT '0',
  `pDef` int(11) NOT NULL DEFAULT '0',
  `mDef` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`doorId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;