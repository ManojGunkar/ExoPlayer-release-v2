#include <stdio.h>
#include <stdlib.h>
#include <SLES/OpenSLES.h>
#define SLEEP(x) /* Client system sleep function to sleep x milliseconds would replace SLEEP macro */
/* External game engine data */

#define EVENT_GUNSHOT (int)0x00000001
#define EVENT_DEATH (int)0x00000002
#define EVENT_FOOTSTEP (int)0x00000003
#define OBJECT_LISTENER (int)0x00000001
#define OBJECT_GUNSHOT (int)0x00000002
#define OBJECT_SCREAM (int)0x00000003
/* External game engine functions */
extern int GAMEGetEvents( void );
extern void GAMEGetLocation( int object, int *x, int *y, int *z);
/*******************************************************************/
#define MAX_NUMBER_INTERFACES 4
/* Checks for error. If any errors exit the application! */
void CheckErr( SLresult res )
{
    if ( res != SL_RESULT_SUCCESS )
    {
/* Debug printing to be placed here */
        exit(1);
    }
}
void Create3DSource( SLEngineItf EngineItf, SLObjectItf OutputMix, SLObjectItf *pPlayer, SLchar *fileName, SLuint32 priority)
{
    SLDataSource audioSource;
    SLDataLocator_URI uri;
    SLDataFormat_MIME mime;
    SLDataSink audioSink;
    SLDataLocator_OutputMix locator_outputmix;
    SLresult res;
    SLboolean required[MAX_NUMBER_INTERFACES];
    SLInterfaceID iidArray[MAX_NUMBER_INTERFACES];
/* Setup the data source structure for the player */
    uri.locatorType = SL_DATALOCATOR_URI;
    uri.pURI = fileName;
    mime.formatType = SL_DATAFORMAT_MIME;
    mime.pMimeType = (SLchar *) "audio/x-wav";
    mime.containerType = SL_CONTAINERTYPE_WAV;
    audioSource.pLocator = (void *)&uri;
    audioSource.pFormat = (void *)&mime;
/* Setup the data sink structure */

    locator_outputmix.locatorType = SL_DATALOCATOR_OUTPUTMIX;
    locator_outputmix.outputMix = OutputMix;
    audioSink.pLocator = (void *)&locator_outputmix;
    audioSink.pFormat = NULL;
/* Set arrays required[] and iidArray[] for 3DLocationItf,
3DSourceItf, 3DDopplerItf, SeekItf interfaces (PlayItf is
implicit).
Not all interfaces are used by all players in this example - in a
real application it is advisable to only request interfaces that
are necessary. */
    required[0] = SL_BOOLEAN_TRUE;
    iidArray[0] = SL_IID_3DLOCATION;
    required[1] = SL_BOOLEAN_TRUE;
    iidArray[1] = SL_IID_3DSOURCE;
    required[2] = SL_BOOLEAN_FALSE; /* Create the player even if Doppler unavailable */
    iidArray[2] = SL_IID_3DDOPPLER;
    iidArray[3] = SL_IID_SEEK;
    required[3] = SL_BOOLEAN_TRUE;
/* Create the 3D player */
    res = (*EngineItf)->CreateAudioPlayer(EngineItf, pPlayer, &audioSource, &audioSink, 4, iidArray, required); CheckErr(res);
    {
        SLObjectItf player = *pPlayer;
/* Set player's priority */
        res = (*player)->SetPriority(player, priority, SL_BOOLEAN_TRUE); CheckErr(res);
/* Realize the player in synchronously */
        res = (*player)->Realize(player, SL_BOOLEAN_TRUE);
        if (res == SL_RESULT_RESOURCE_ERROR )
        {
/* Ignore resource errors, they're handled elsewhere. */
        }
        else
        {
            CheckErr(res);
        }
    }
}
/* Play create a 3D source and spin it around the listener */
void TestAdvanced3D( SLObjectItf sl )
{
    SLEngineItf EngineItf;
    SL3DCommitItf commitItf;
    SLresult res;
    SLObjectItf gunshot, scream, footstep, torch;
    SLPlayItf playItf;
    SL3DLocationItf locationItf;
    SL3DSourceItf sourceItf;
    SL3DDopplerItf dopplerItf;
    SLObjectItf listener;
    SLObjectItf OutputMix;
    SLboolean required[MAX_NUMBER_INTERFACES];
    SLInterfaceID iidArray[MAX_NUMBER_INTERFACES];
    SLuint32 state;
    int i;
/* Get the SL Engine Interface which is implicit */
    res = (*sl)->GetInterface(sl, SL_IID_ENGINE, (void*)&EngineItf); CheckErr(res);
/* Initialize arrays required[] and iidArray[] */
    for (i=0;i<MAX_NUMBER_INTERFACES;i++)
    {
        required[i] = SL_BOOLEAN_FALSE;
        iidArray[i] = SL_IID_NULL;
    }
/* Get the commit interface and for efficiency reasons set into deferred mode. */
    res = (*sl)->GetInterface(sl, SL_IID_3DCOMMIT, (void*)&commitItf); CheckErr(res);
    (*commitItf)->SetDeferred(commitItf, SL_BOOLEAN_TRUE);
/* Create Output Mix object to be used by player - no interfaces required */
    res = (*EngineItf)->CreateOutputMix(EngineItf, &OutputMix, 0, iidArray, required); CheckErr(res);
/* Realizing the Output Mix object in synchronous mode. */
    res = (*OutputMix)->Realize(OutputMix, SL_BOOLEAN_FALSE); CheckErr(res);
/* Create 3D listener. */
    required[0] = SL_BOOLEAN_TRUE;
    iidArray[0] = SL_IID_3DLOCATION;
    res = (*EngineItf)->CreateListener(EngineItf, &listener, 1, iidArray, required); CheckErr(res);
/* Realizing the listener object in synchronous mode. */
    res = (*listener)->Realize(listener, SL_BOOLEAN_FALSE); CheckErr(res);
/* Create four players with differing priorities. Higher priorities
are used for sound effects that must be heard by the game player,
whereas lower priorities are assigned to sound effects that make
 the game sound better but are not required to appreciate
the game. */
    Create3DSource(EngineItf, OutputMix, &gunshot, "gunshot.wav", SL_PRIORITY_HIGH);
    Create3DSource(EngineItf, OutputMix, &scream, "scream.wav", SL_PRIORITY_NORMAL);
    Create3DSource(EngineItf, OutputMix, &footstep, "footstep.wav", SL_PRIORITY_NORMAL);
    Create3DSource(EngineItf, OutputMix, &torch, "torch.wav", SL_PRIORITY_LOW);
    (*gunshot)->GetState(gunshot, &state);
    if (state == SL_OBJECT_STATE_REALIZED)
    {
/* Set the gun shot's 3D source properties */
        res = (*gunshot)->GetInterface(gunshot, SL_IID_3DSOURCE, (void*)&sourceItf); CheckErr(res);
/* Set rolloff model to linear */
        (*sourceItf)->SetRolloffModel(sourceItf, SL_ROLLOFFMODEL_LINEAR); CheckErr(res);
/* Exaggerate the gunshot's rolloff */
        (*sourceItf)->SetRolloffFactor(sourceItf, 1500); CheckErr(res);
/* Add Doppler to the gunshot, if possible */
        res = (*gunshot)->GetInterface(gunshot, SL_IID_3DDOPPLER, (void*)&dopplerItf);
        if (res != SL_RESULT_SUCCESS)
        {
/* Doppler not available - not crucial though */
        }
        else
        {
            SLVec3D vec;
/* Exaggerate gunshot's Doppler */
            (*dopplerItf)->SetDopplerFactor(dopplerItf, 2000); CheckErr(res);
/* Set gunshot's velocity to move away from the listener */
            vec.x = 0; vec.y = 0; vec.z = -1000;
            (*dopplerItf)->SetVelocityCartesian(dopplerItf, &vec); CheckErr(res);
        }
    }
    else
    {
/* Exit - game isn't viable without gunshot */
        exit(1);
    }
    (*footstep)->GetState(footstep, &state);
    if (state == SL_OBJECT_STATE_REALIZED)
    {
/* Set foot step's 3D source properties */
        res = (*footstep)->GetInterface(footstep, SL_IID_3DSOURCE, (void*)&sourceItf); CheckErr(res);
        /* Set foot steps as head relative - as the listener moves, so
do the foot steps. */
        res = (*sourceItf)->SetHeadRelative(sourceItf, SL_BOOLEAN_TRUE); CheckErr(res);
    }
    else
    {
/* Exit - game isn't viable without gunshot */
        exit(1);
    }
    (*torch)->GetState(torch, &state);
    if (state == SL_OBJECT_STATE_REALIZED)
    {
        SLVec3D vec;
        SLSeekItf seekItf;
        res = (*torch)->GetInterface(torch, SL_IID_PLAY, (void*)&playItf); CheckErr(res);
        res = (*torch)->GetInterface(torch, SL_IID_3DLOCATION, (void *)&locationItf); CheckErr(res);
        res = (*torch)->GetInterface(torch, SL_IID_SEEK, (void*)&seekItf); CheckErr(res);
/* Position the torch somewhere in 3D space */
        vec.x = 30000; vec.y = 0; vec.z = -26000;
        (*locationItf)->SetLocationCartesian(locationItf, &vec); CheckErr(res);
/* Play torch constantly looping */
        (*seekItf)->SetLoop(seekItf, SL_BOOLEAN_TRUE, 0, SL_TIME_UNKNOWN); CheckErr(res);
/* Commit 3D settings before playing */
        (*commitItf)->Commit(commitItf);
        (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_PLAYING); CheckErr(res);
    }
    else
    {
/* Torch isn't available. Could try realizing again but torch sound
effect isn't crucial to game play. */
    }
/* Main game loop */
    {
        int dead = 0;
        while (!dead)
        {
            int gameEvent;
            SLVec3D vec;
/* Handle game events */
            gameEvent = GAMEGetEvents();
            switch( gameEvent )
            {
                case EVENT_GUNSHOT:
/* Fire gun shot */
                    res = (*gunshot)->GetInterface(gunshot, SL_IID_PLAY, (void *)&playItf); CheckErr(res);
                    (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_PLAYING); CheckErr(res);
                    break;
                case EVENT_DEATH:
/* Player has been shot, scream! */
                    res = (*scream)->GetInterface(scream, SL_IID_PLAY, (void *)&playItf); CheckErr(res);
                    (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_PLAYING); CheckErr(res);
                    dead = !dead;
                    break;
                case EVENT_FOOTSTEP:
/* Play foot steps */
                    res = (*footstep)->GetInterface(footstep, SL_IID_PLAY, (void *)&playItf); CheckErr(res);
                    (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_PLAYING); CheckErr(res);
                    break;
            }
/* Update location of gun shot, scream and listener based on
information from game engine. No need to update foot steps
as they are head relative (i.e. move with the listener). */
            GAMEGetLocation(OBJECT_LISTENER, &vec.x, &vec.y, &vec.z);
            res = (*listener)->GetInterface(listener, SL_IID_3DLOCATION, (void *)&locationItf); CheckErr(res);
            (*locationItf)->SetLocationCartesian(locationItf, &vec); CheckErr(res);
            GAMEGetLocation(OBJECT_GUNSHOT, &vec.x, &vec.y, &vec.z);
            res = (*gunshot)->GetInterface(gunshot, SL_IID_3DLOCATION, (void *)&locationItf); CheckErr(res);
            (*locationItf)->SetLocationCartesian(locationItf, &vec); CheckErr(res);
            GAMEGetLocation(OBJECT_SCREAM, &vec.x, &vec.y, &vec.z);
            res = (*scream)->GetInterface(scream, SL_IID_3DLOCATION, (void *)&locationItf); CheckErr(res);
            (*locationItf)->SetLocationCartesian(locationItf, &vec); CheckErr(res);
/* Commit 3D settings otherwise 3D positions will not be updated. */
            (*commitItf)->Commit(commitItf);
            SLEEP(10);
        }
    }
/* Wait until scream finished before exiting */
    (*scream)->GetState(scream, &state);
    if (state == SL_OBJECT_STATE_REALIZED)
    {
        res = (*scream)->GetInterface(scream, SL_IID_PLAY, (void *)&playItf); CheckErr(res);
        do
        {
            (*playItf)->GetPlayState(playItf, &state); CheckErr(res);
            SLEEP(10);
        } while (state == SL_PLAYSTATE_PLAYING);
    }
/* Destroy the players */
    (*gunshot)->Destroy(gunshot);
    (*scream)->Destroy(scream);
    (*footstep)->Destroy(footstep);
    (*torch)->Destroy(torch);
/* Destroy the listener object */
    (*listener)->Destroy(listener);
/* Destroy Output Mix object */
    (*OutputMix)->Destroy(OutputMix);
}
int sl_main( void )
{
    SLresult res;
    SLObjectItf sl;
    SLEngineOption EngineOption[] = {
            (SLuint32) SL_ENGINEOPTION_THREADSAFE,
            (SLuint32) SL_BOOLEAN_TRUE,
            (SLuint32) SL_ENGINEOPTION_MAJORVERSION, (SLuint32) 1,
            (SLuint32) SL_ENGINEOPTION_MINORVERSION, (SLuint32) 1
    };
    SLboolean required = SL_BOOLEAN_TRUE;
    SLInterfaceID iid = SL_IID_3DCOMMIT;
/* Create an engine with the 3DCommit interface present */
    res = slCreateEngine( &sl, 3, EngineOption, 1, &iid, &required); CheckErr(res);
/* Realizing the SL Engine in synchronous mode. */
    res = (*sl)->Realize(sl, SL_BOOLEAN_FALSE); CheckErr(res);
    TestAdvanced3D(sl);
    /* Shutdown OpenSL ES */
    (*sl)->Destroy(sl);
    exit(0);
}