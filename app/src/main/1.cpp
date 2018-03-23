while(gri_node->update() && ((draw_enabled && draw_update()) || (!draw_enabled)) )
{
  gri::profiler profile_MainLoop("Main Loop");
  if (ratslam->get_current_frame_count() % 100 == 0)
    cout << "RatSLAM progress: " << ratslam->get_current_frame_count() << endl;

  if (irat_robot->is_fresh())
  {
    printf("f");
    fflush(stdout);


    ratslam->set_view_rgb(irat_robot->get_camera()->get_rgb_data());
    ratslam->set_is_docked((irat_robot->get_charging()?1:0));
    ratslam->set_odom(irat_robot->get_trans_vel(), irat_robot->get_rot_vel());

    current_time += irat_robot->get_delta_time();
    ratslam->set_delta_time(irat_robot->get_delta_time());
    ratslam->process();


  }
  else
  {
    printf("n");
    fflush(stdout);
  }



  if (ratslam->get_current_frame_count() % 1000 == 0)
  {
    gri::profiler::generate_report();
    gri::profiler::restart_all();
  }

  gri_node->do_log();

  profile_MainLoop.stop();
  gri_node->sleep();


  if (ratslam->get_current_frame_count() !=0 && ratslam->get_current_frame_count() == reset_ratslam)
  {
    delete ratslam;
    if (!load_ratslam_filename.empty())
    {
      std::ifstream ifs(load_ratslam_filename.c_str());
      boost::archive::text_iarchive ia(ifs);
      ia >> ratslam;  
    } else {
      ratslam = new ratslam::Ratslam(ratslam_settings);
    }
    draw_update_ratslam_ptr(ratslam);
  }
}
