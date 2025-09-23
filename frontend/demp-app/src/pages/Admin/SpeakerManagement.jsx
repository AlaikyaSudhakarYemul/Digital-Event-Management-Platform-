import React from 'react';
 
const SpeakerManagement = ({
  speakerForm,
  handleSpeakerChange,
  handleSpeakerSubmit,
  speakers,
  handleSpeakerEdit,
  handleSpeakerDelete,
  editingSpeakerId,
  speakerMessage
}) => {
  return (
    <div>
      <h2 className="text-xl font-semibold mb-4">
        {editingSpeakerId ? 'Edit Speaker' : 'Add Speaker'}
      </h2>
      <form onSubmit={handleSpeakerSubmit} className="space-y-4">
        <input
          type="text"
          name="name"
          value={speakerForm.name}
          onChange={handleSpeakerChange}
          placeholder="Enter speaker name"
          className="w-full px-4 py-2 rounded bg-gray-700 border border-gray-600"
        />
        <input
          type="text"
          name="bio"
          value={speakerForm.bio}
          onChange={handleSpeakerChange}
          placeholder="Enter speaker bio"
          className="w-full px-4 py-2 rounded bg-gray-700 border border-gray-600"
        />
        <button
          type="submit"
          className="w-full bg-cyan-500 hover:bg-cyan-600 text-white font-semibold py-2 px-4 rounded-lg"
        >
          {editingSpeakerId ? 'Update Speaker' : 'Add Speaker'}
        </button>
        {speakerMessage && (
          <p className="text-center text-sm mt-2 text-yellow-400">{speakerMessage}</p>
        )}
      </form>
 
      <hr className="my-6 border-gray-600" />
 
      <h2 className="text-xl font-semibold mb-4">Existing Speakers</h2>
      <ul className="space-y-2">
        {speakers.map((spk) => (
          <li key={spk.speakerId} className="bg-gray-700 p-3 rounded flex justify-between items-start">
            <div>
              <p className="font-bold">{spk.name}</p>
              <p className="text-sm text-gray-300">{spk.bio}</p>
            </div>
            <div className="space-x-2">
              <button
                onClick={() => handleSpeakerEdit(spk)}
                className="bg-yellow-500 hover:bg-yellow-600 text-white px-3 py-1 rounded"
              >
                Edit
              </button>
              <button
                onClick={() => handleSpeakerDelete(spk.speakerId)}
                className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded"
              >
                Delete
              </button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
};
 
export default SpeakerManagement;
 