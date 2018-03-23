#include <jni.h>
#include <android/asset_manager.h>

#include "forlog.h"


#include "ratslam/Experience_Map.h"
#include "ratslam/Ratslam.hpp"

#include "yuv2rgb/yuv2rgb.h"
#include "gri/gri_util.h"

#include <boost/property_tree/ini_parser.hpp>



using boost::property_tree::ptree;

#define CONFIG_FILE "config.txt"

static jclass    claz;
static jfieldID  x_id ;
static jfieldID  y_id ;
static jmethodID ctr ;


extern "C" {

JNIEXPORT jlong
JNICALL Java_com_example_sudhir_ratslamlib_RatSlamInterFace_initRatslam(JNIEnv *env, jobject instance, jstring path) ;

JNIEXPORT void JNICALL Java_com_example_sudhir_ratslamlib_RatSlamInterFace_setViewrgb(JNIEnv
* env, jobject instance, jlong slam, jbyteArray rgb , jint length);

JNIEXPORT void JNICALL Java_com_example_sudhir_ratslamlib_RatSlamInterFace_setViewrgbAndProcess(JNIEnv * env, jobject instance, jlong slam, jbyteArray rgb, jint length );

JNIEXPORT void JNICALL Java_com_example_sudhir_ratslamlib_RatSlamInterFace_setOdom(JNIEnv
* env, jobject instance, jlong slam , jfloat vel, jfloat rad );

JNIEXPORT void JNICALL Java_com_example_sudhir_ratslamlib_RatSlamInterFace_setDeltatime(JNIEnv
* env, jobject instance, jlong slam, jdouble deltaTime  );


JNIEXPORT void JNICALL Java_com_example_sudhir_ratslamlib_RatSlamInterFace_slamProcess(JNIEnv
* env, jobject instance, jlong slam);

JNIEXPORT jcharArray JNICALL Java_com_example_sudhir_ratslamlib_RatSlamInterFace_yuv2rgb (JNIEnv
* env, jobject instance, jchar yuv []);

JNIEXPORT jobjectArray  JNICALL Java_com_example_sudhir_ratslamlib_RatSlamInterFace_printExpMap (JNIEnv * env, jobject instance, jlong slam);

JNIEXPORT jobject JNICALL Java_com_example_sudhir_ratslamlib_RatSlamInterFace_getExpNow (JNIEnv * env, jobject instance, jlong slam);

}



void Java_com_example_sudhir_ratslamlib_RatSlamInterFace_setDeltatime(JNIEnv * env, jobject instance, jlong slam, jdouble deltaTime  )
{

    ratslam::Ratslam *slamPtr = reinterpret_cast<ratslam::Ratslam *> (slam);

    slamPtr->set_delta_time (deltaTime);

}



void Java_com_example_sudhir_ratslamlib_RatSlamInterFace_setViewrgb(JNIEnv * env, jobject instance, jlong slam, jbyteArray rgb, jint length )
{
    if (length > 0)
    {
        ratslam::Ratslam *slamPtr = reinterpret_cast<ratslam::Ratslam *> (slam);
        jboolean isCopy = false;

        jbyte *buf = (jbyte *) malloc ( sizeof (jbyte)*length );
 
        env->GetByteArrayRegion(rgb, 0, length, buf);

        slamPtr->set_view_rgb ( (unsigned char*) buf );

    }

}


void Java_com_example_sudhir_ratslamlib_RatSlamInterFace_setViewrgbAndProcess(JNIEnv * env, jobject instance, jlong slam, jbyteArray rgb, jint length )
{

    if (length > 0)
    {
        ratslam::Ratslam *slamPtr = reinterpret_cast<ratslam::Ratslam *> (slam);
        jboolean isCopy = false;
        jbyte* data = env->GetByteArrayElements(rgb, &isCopy);

        slamPtr->set_view_rgb ( reinterpret_cast<const unsigned char*>(data) );
        slamPtr->process();

        env->ReleaseByteArrayElements(rgb, data, JNI_ABORT);

    }

}

jlong Java_com_example_sudhir_ratslamlib_RatSlamInterFace_initRatslam(JNIEnv *env, jobject instance, jstring path)
{

// TODO

    LOGD("==================== RAT SLAM Init: START ============ \n");

    boost::property_tree::ptree settings;
    boost::property_tree::ptree ratslam_settings;
    boost::property_tree::ptree default_setting;

    //memset (&default_setting,sizeof(default_setting),0);

    const char *p = env->GetStringUTFChars (path, NULL);

    read_ini (p, settings);

    gri::get_setting_child (ratslam_settings, settings, "ratslam");

    ratslam::Ratslam *slam = new ratslam::Ratslam(ratslam_settings);

    long handle = reinterpret_cast<jlong> (slam);
    LOGD("==================== \"RAT SLAM Init: END  ============ \n");


    return handle; //reinterpret_cast<jlong> (slam);
}

void Java_com_example_sudhir_ratslamlib_RatSlamInterFace_setOdom(JNIEnv * env , jobject instance, jlong slam , jfloat vel, jfloat rad)
{

// TODO
  ratslam::Ratslam *slamPtr = reinterpret_cast<ratslam::Ratslam *> (slam);

  slamPtr->set_odom (vel,rad);
}

void Java_com_example_sudhir_ratslamlib_RatSlamInterFace_slamProcess(JNIEnv * env, jobject instance, jlong slam)
{

    ratslam::Ratslam *slamPtr = reinterpret_cast<ratslam::Ratslam *> (slam);

    slamPtr->process();
}

jobject Java_com_example_sudhir_ratslamlib_RatSlamInterFace_getExpNow (JNIEnv * env, jobject instance, jlong slam)
{
    ratslam::Ratslam *ratslam = reinterpret_cast<ratslam::Ratslam *> (slam);

    ratslam::Experience_Map *experience_map = ratslam->get_experience_map();

    int current_experience_id = experience_map->get_current_id();

    ratslam::Experience * robot_experience = experience_map->get_experience(current_experience_id);

    // LOGD ("MAP: Robot at %f, %f\n", robot_experience->x_m, robot_experience->y_m);

    claz   = env->FindClass("com/example/sudhir/ratslamlib/ExpMap");
    x_id   = env->GetFieldID (claz, "x", "D");
    y_id   = env->GetFieldID (claz, "y", "D");
    ctr    = env->GetMethodID(claz, "<init>", "()V");



    jobject exp = env->NewObject(claz,ctr);
    env->SetDoubleField (exp,x_id,robot_experience->x_m);
    env->SetDoubleField (exp,y_id,robot_experience->y_m);

    return exp;


}

jobjectArray Java_com_example_sudhir_ratslamlib_RatSlamInterFace_printExpMap (JNIEnv * env, jobject instance, jlong slam)
{


  ratslam::Ratslam *ratslam = reinterpret_cast<ratslam::Ratslam *> (slam);

  ratslam::Experience_Map *experience_map = ratslam->get_experience_map();

  int current_experience_id = experience_map->get_current_id();
  int experience_count = experience_map->get_num_experiences();

  ratslam::Experience * robot_experience = experience_map->get_experience(current_experience_id);

  // LOGD ("MAP: Robot at %f, %f\n", robot_experience->x_m, robot_experience->y_m);

  claz   = env->FindClass("com/example/sudhir/ratslamlib/ExpMap");
  x_id   = env->GetFieldID (claz, "x", "D");
  y_id   = env->GetFieldID (claz, "y", "D");
  ctr    = env->GetMethodID(claz, "<init>", "()V");


  jobjectArray expmap = env->NewObjectArray ( experience_count, claz, NULL);

  jobject exp;

  for (int i = 0; i < experience_count; i++)
  {
    ratslam::Experience * experience1 = experience_map->get_experience(i);

    exp = env->NewObject(claz,ctr);
    env->SetDoubleField (exp,x_id,experience1->x_m);
    env->SetDoubleField (exp,y_id,experience1->y_m);

    env->SetObjectArrayElement(expmap,i,exp);

    // nodes in the experience map
    // LOGD ("MAP: Experience at %f, %f\n", experience1->x_m, experience1->y_m);

    // for (int j = 0; j < experience1->links_from.size(); j++)
    // {
      // links in the experience map
      // ratslam::Link * link = experience_map->get_link( experience1->links_from[j]);
      // ratslam::Experience * experience2 = experience_map->get_experience(link->exp_to_id);

      // LOGD ("MAP: Link to experience at %f, %f\n", experience2->x_m, experience2->y_m);

    // }
  }


  return expmap;
}


jcharArray  Java_com_example_sudhir_ratslamlib_RatSlamInterFace_yuv2rgb (JNIEnv * env, jobject instance, jbyteArray yuv)
{
  //jbyte* bufferPtr = (*env)->GetByteArrayElements(env, yuv, NULL);

  //jsize lengthOfArray = (*env)->GetArrayLength(env, yuv);


  //(*env)->ReleaseByteArrayElements(env, yuv, bufferPtr, 0);

   return NULL;
}
