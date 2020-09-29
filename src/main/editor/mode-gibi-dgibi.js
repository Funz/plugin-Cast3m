ace.define("ace/mode/gibi_highlight_rules",["require","exports","module","ace/lib/oop","ace/lib/lang","ace/mode/text_highlight_rules"], function(require, exports, module) {
"use strict";

var oop = require("../lib/oop");
var lang = require("../lib/lang");
var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

var gibiHighlightRules = function() {

    var keywordMapper = this.createKeywordMapper({
        "constant.language": 
            "TRUE VRAI FALSE FAUX NULL SPACE LIGNE LIGNES",
        "storage.type":
            "ENTIER ENTI FLOTTANT FLOT LISTENTI LISTREEL MOT MOTS LISTMOTS LOGIQUE TABLE EVOLUTIO TEXTE TEXT", 
        "keyword.control":
            "SI SINON SINO FINSI FINS REPETER REPE FIN QUIT DEBPROC FINPROC FINP DEBMETH DEBM FINMETH FINM",
        "support.function":
            "ABS ACCDCHI1 ACCDCHI2 ACCEVITE ACIER ACOS ACQU ACT3 ACTI ACTI3 ACTUSAT1 ACTUSAT2 ACTUSAT3 ADET ADVE AFCO AFFI AFFICHE AIDE AJU1 AJU2 AJUSTE ALEA AMOR ANALYSER ANIME ANIMGKS ANLIMTRE ANNOIMP ANNU ANTI APPU ARCGAU ARET ARGU ASIN ASPARAM ASSI ATG AUTOPILO AVCT "+
            "BALOURD BARY BASE BGMO BIF BILI_EFZ BILI_MOY BIOT BIOVOL BLOQ BMTD BOA BRUCHE BRUI BSIG "+
            "CALACTIV CALCDISP CALCP CALCTRAC CALCULER CALLM CALMU CALP CAMPBELL CAPA CAPI CARA CBLO CCDONCHI. CCON CER3 CERC CFL CFND CH_THETA CH_THETX CH2CLIM CHAI CHAMINT CHAN CHANOLII CHANUOBJ CHANVCOM CHANVESP CHANVSOS CHAR CHARTHER CHAU CHDCLIM CHI1 CHI2 CHITRNSP CHOC CHOI CHPO CHPR0 CHSP CHTGAU CHTITR CINEMA CINEMB CINIMOD CLMI CLST CMCT CMOY CNEQ COAC CODENORM COLI COLLER COLLER1 COMB COMM COMP COMT CONC COND CONDENS CONF CONG CONN CONT CONTSEG3 CONV CONVT COOR COPI COQ2MAS CORI CORMAN CORMASSE COS "+ "COSH COSI COTE COUL COUP COUPLER COUR COUR3D COURSPEC COUT CREER_3D CRIT CRITLOC CSON CTOD CUBP CUBT CVOL "+
            "DALL DANS DARCYSAT DARCYTRA DBIT DCOV DDFOUR DEADFONC DEADJACO DEADKTAN DEADRESI DEADUTIL DEBI DEBM DEBP DEBU DECO DECONV DECONV3D DEDANS DEDO DEDU DEDUADAP DEFO DEG3 DENS DEPB DEPI DEPL DEPOU DESCOUR DESS DESTRA DETO DETR DEVE DFDT DFER DFOU DGSI DIAD DIAG DIFF DIFFANIS DIME DIMN DIRI DIVU DMMU DMTD DOMA DONCHI1 DONCHI2 DREXUS DROI DSPR DUDW DUPONT2 DYNAMIC DYNAMOD2 DYNAMOD3 DYNAMODE DYNE "+
            "EC8ACSIS ECFE ECHI ECHIMP ECOU EGA ELAS ELECNEUT ELEM ELFE ELIM ELNO ELST ENCEINTE ENER ENERMODE ENLE ENRICHIS ENSE ENTI ENVE EPAIFUT EPSI EPTH EQEX EQPR EQUI ERF ERRE ET ETG EVOL EXAC EXCE EXCF EXCI EXCO EXCP EXEC EXECRXT EXIC EXIS EXP EXPLORER EXTC EXTE EXTR "+
            "F_S2PI FACE FACTORIE FANT FCOURANT FDENS FDT FFOR FIABILI FILT FILTREKE FIMP FIN FINM FINP FINS FINVREPA FION FISS FLAM FLAMBAGE FLOT FLUX FOFI FONC FOR_CONT FORBLOC FORC FORM FORNOD FOUR2TRI FPA FPAL FPT FPU FREPART FREQPERI FRIG FRON FRONABS FROT FSUR FUIT "+
            "G_CALCUL G_THETA G_THETA1 GAM1 GANE GDFLIM1 GENE GENJ GMV GRAD GRAF GREE GRESP GYRO "+
            "H_B HANN HASOFER HAUBAN HDEB HERI HIST HOMO HOOK HOTA HP_PRO HRAYO HRCAV HT_PRO HTC_CHBW HTC_PER HTC_WTR HTC_WWW HTCTRAN HVIT HYBP "+
            "IDBHT IDENTI IDLI IFRE IJET IMAGES IMPCHI1 IMPCHI2 IMPE IMPF IMPO IN_MINI INCL INCREME INCREME2 INDE INDI INDIBETA INDIOBJE INDUCTIO INFO INICHI1 INICHI2 INICHIMI INIMUR ININLIN INITVF INSE INSI INT_COMP INTE INTEFMH INTG INVA INVE IPOL ISOV ITER ITRC "+
            "JACO JEU JONC "+
            "K_PRO KBBT KCHA KCHT KCOT KCTR KDIA KDME KDMI KDOM KENT KEPSILON KFCE KFPA KFPT KHIS KLNO KLOP KMAB KMAC KMBT KMCT KMF KMTP KNRF KONV KOPS KP KPRO KR_PRO KRED KRES KRESP KSIG KSOF KTAN KUET KVOL KWEIB1 "+
            "LAPL LAPN LCH2CLIM LCH2DELP LCH2EPS LCH2FION LCH2IAFF LCH2IMPR LCH2ITMA LCH2ITSO LCH2LOGC LCH2MDEL LCH2NFI LCH2NITE LCH2NTY4 LCH2PREP LCH2SORT LCH2TEMP LCH2TOT LECT LESPCOMP LESPITYP LESPLOGK LESPSOE LEVM LIAI LIBDD LICHXMX LICOCHAR LICOMNOM LIESPECE LIGN LILIDEN LILIECH LIMEMECA LINBIDEN LINVCOMP LIRE LIREFLOT LIRSOSO LIST LITEMPER LOG LOGK LSOSFRAC LSOSSOLI LSQF LTL LUMP "+
            "M_DAMP_K M_DAMPIN MAG_NLIN MAGN MAILSTRU MANU MAPP MASQ MASS MATE MATP MAX1 MAXI MAYOTO MDIA MDNRIS MDRECOMB MEC1 MEC2 MEC3 MECA MENA MENU MESM MESS MESU METH MHYB MINI MOCA MOCU MODE MOIN MOME MONTAGNE MOT MOTA MOTS MOYESPEC MPRO MREM MULC MULT MULTIDEC MULTIREC MUTU "+
            "NATAF NAVI NAVIER NBEL NBNO NEG NEUT NEWMARK NLIN NLOC NNOR NOCOMCHI NOEL NOESPCHI NOEU NOMC NON NONLIN NORM NORMALIM NORV1 NOTI NOUV NS NSCLIM NSKE NTAB NUAG "+
            "OBJE OBTE ONDE OPTI ORDO ORIE ORTH OSCI OTER OU OUBL OUVFISS "+
            "PARA PARASTAT PARC PARMCHI2 PART PAS_ETAT PAS_SAUV PAS_VERM PASAPAS PAVE PECHE PENT PERM PERT PFLUAGE PHAJ PHASAGE PICA PILE PJBA PLAC PLUS PMAT PMIX PMPB POIN POINTCYL POINTSPH POLA POLYNO POSS POSTDDI POSTDDI1 POT_SCAL POT_VECT POUT2MAS PPRE PREC PREPAENC PRES PRET PRIM PRIN PRNS PROB PROBABRS PROBDENS PRODT PROG PROI PROJ PROP PROPAG PSATT PSCA PSIP PSMO PSRS PVEC "+
            "QOND QUADRATU QUEL QUIT QULX "+
            "RACC RACP RAFF RAFT RAMBERG RAVC RAY RAYE RAYN RDIV REAC RECENTRE RECO RECOMPOM RECOMPOS REDU REFE REGE REGL RELA REMP REPART REPE REPIX RESEAU RESI RESO RESO_ASY RESP RESPOWNS RESPOWSP REST RESU RETO RETRAIT RETSAT RIGI RIMP ROSENT ROTA RSET RTEN RVSAT RVST2 "+
            "SAIS SATUTILS SAUF SAUT SAUV SEIS SENS SGE SI SIAR SIF SIGM SIGN SIGNCORR SIGNDERI SIGNENVE SIGNSYNT SIGS SILAM SIMP SIN SINH SINO SISSIB SMTP SOLS SOLVEFMH SOLVVF SOMM SOMT SORE SORT SOUR SPAL SPO SPON SPPLANC SQTP SSCH SSTE STRU SUIT SUPE SURF SYME SYMT SYNT "+
            "T_IPOL T_PITETA TABL TAGR TAIL TAKM_EFZ TAKM_MOY TAN TANH TASS TCNM TCRR TEMP TENSION TEXT TFR TFRI THERMIC THET TIRE TITR TOIM TOTE TOUR TRAC TRAC3D TRAC3D_2 TRACHIS TRACHIT TRACMECA TRACTUFI TRADUIRE TRAJ TRAN TRANGEOL TRANSFER TRANSGEN TRANSIT0 TRANSIT1 TRANSIT2 TRANSIT3 TRANSLIN TRANSNON TRES TRIA TRIE TRTRAJEC TSCA TYPE "+
            "UNILATER UNILSSUP UNPAS UPDAEFMH UPDAVF UTIL "+
            "VALE VALNOM ZERO ZIGZAG ZLEG  ",
    }, "text", false, " ");

    this.$rules = {
        "start" : [
            {token : "comment.line.character",  regex : /^\s?\*.+$/},
            {token : "string", regex : "'", next  : "string"},
            {token : "paren.lparen", regex : "[\\[({]"},
            {token : "paren.rparen", regex : "[\\])}]"},
            {token : "constant.numeric", regex: "[+-]?\\d+(\.\\d+)?(e[-]?\\d+)?\\b"},
            {      token : ["storage.type", "text", "entity.name.function"],
                   regex : "(DEBP|DEBM)(\\s+)([a-zA-Z_][a-zA-Z0-9_]*)",
                   next : "procmethdef"
            },
            {      token : ["keyword.control", "text", "variable.parameter", "text", "variable.other"],
                   regex : "(REPE)(\\s+)([a-zA-Z_][a-zA-Z0-9_]*)?(\\s+)([a-zA-Z_][a-zA-Z0-9_]*)?"
            },
            {token : "variable.parameter", regex : /&[a-zA-Z][a-zA-Z0-9_]?/},
            {      token : ["keyword.control", "text", "variable.parameter", "punctuation.operator"],
                   regex : "(FIN)(\\s+)([a-zA-Z_][a-zA-Z0-9_]*)?(;)"
            },
            {      token : ["keyword.control", "text", "variable.other"],
                   regex : "(FINP|FINM)(\\s+)([a-zA-Z_][a-zA-Z0-9_]*)?"
            },
            {      token : ["variable.other", "text", "keyword.operator"],
                   regex : "^([a-zA-Z_][a-zA-Z0-9_]*)(\\s+)?(=)"
            },
             {      token : ["variable.other"],
                   regex : "^(\\s?[a-zA-Z_][a-zA-Z0-9_]*\\s?\\.)"
            },
            {token : "keyword.operator", regex : /ET |OU |EGA |NEG |NON |>EG|<EG|\*\*|[*\/+\-<>=\.]/},
            {token : keywordMapper, regex : "\\b\\w+\\b"},
            {token : "punctuation.operator", regex : "\\;"},
            {caseInsensitive: true}
        ],
        "procmethdef" : [
            {token : "variable.parameter",   regex : /[A-Za-z][A-Za-z0-9_]+/},
            {token : "storage.type",   regex : /\*[A-Za-z]+/},
            {token : "punctuation.operator", regex : ";",     next  : "start"},
            {defaultToken : "text"}
        ],
        "string" : [
            {token : "constant.language.escape",   regex : "''"},
            {token : "string", regex : "'",     next  : "start"},
            {defaultToken : "string"}
        ],
        "complexVar" : [
            {token : "keyword.operator", regex : "=",     next  : "start"},
            {defaultToken : "text"}
        ]
    };
};

oop.inherits(gibiHighlightRules, TextHighlightRules);

exports.gibiHighlightRules = gibiHighlightRules;
})

,ace.define("ace/mode/gibi",["require","exports","module","ace/lib/oop","ace/lib/lang","ace/mode/text","ace/mode/gibi_highlight_rules"],function(require,exports,module){
	"use strict";
	var oop=require("../lib/oop");
   	var TextMode = require("./text").Mode;
   	var gibiHighlightRules = require("./gibi_highlight_rules").gibiHighlightRules;
   	
	var Mode = function(){
      this.HighlightRules = gibiHighlightRules;
      this.$behaviour = this.$defaultBehaviour;
   };
   oop.inherits(Mode, TextMode);
     
   (function() {
      this.$id = "ace/mode/gibi";
   }).call(Mode.prototype);
   exports.Mode = Mode;
});                (function() {
                    ace.require(["ace/mode/gibi"], function(m) {
                        if (typeof module == "object" && typeof exports == "object" && module) {
                            module.exports = m;
                        }
                    });
                })();


