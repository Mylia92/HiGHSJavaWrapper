/* swig_java_highs.i */
%module highs
%{
#include "lp_data/HStruct.h"
#include "lp_data/HConst.h"
#include "util/HighsInt.h"
#include "model/HighsModel.h"
#include "lp_data/HighsStatus.h"
#include "Highs.h"
%}


%include "std_vector.i"
namespace std {
   %template(DoubleVector) vector<double>;
}

%include "carrays.i"
%array_class(double, DoubleArray);
%array_class(int, IntegerArray);
%include "std_string.i"
%include "lp_data/HStruct.h"
%include "lp_data/HConst.h"
%include "util/HighsInt.h"
%include "model/HighsModel.h"
%include "lp_data/HighsStatus.h"
%include "Highs.h"